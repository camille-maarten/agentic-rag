import {useState, useEffect, useRef, RefObject} from 'react';
import { MessageCircle, Activity, Trash2, RefreshCw } from 'lucide-react';

const API_BASE = 'http://localhost:8080/api/audit/topic';
const WEBSOCKET_URL = 'ws://localhost:8080/chat/{maarten.vandeperre}';

const AUDIT_ENDPOINTS = [
    { key: 'recipe-request-received', label: 'Recipe Request Received', color: 'bg-blue-50 bord er-blue-200' },
    { key: 'recipe-request-approved', label: 'Recipe Request Approved', color: 'bg-green-50 border-green-200' },
    { key: 'recipe-request-rejected', label: 'Recipe Request Rejected', color: 'bg-red-50 border-red-200' },
    { key: 'recipe-validation-received', label: 'Recipe Validation Received', color: 'bg-purple-50 border-purple-200' },
    { key: 'recipe-validation-approved', label: 'Recipe Validation Approved', color: 'bg-emerald-50 border-emerald-200' },
    { key: 'recipe-validation-rejected', label: 'Recipe Validation Rejected', color: 'bg-pink-50 border-pink-200' },
    { key: 'recipe-postprocessor-received', label: 'Recipe Post-processor', color: 'bg-yellow-50 border-yellow-200' },
    { key: 'recipe-postprocessor-approved', label: 'Recipe Post-processor Approved', color: 'bg-teal-50 border-teal-200' },
    { key: 'recipe-postprocessor-rejected', label: 'Recipe Post-processor Rejected', color: 'bg-orange-50 border-orange-200' },
    { key: 'user-message-received', label: 'User Message Received', color: 'bg-indigo-50 border-indigo-200' }
];

const KafkaAuditUI = () => {
    const [activeTab, setActiveTab] = useState('chat');
    const [messages, setMessages] = useState([]);
    const [inputMessage, setInputMessage] = useState('');
    const [auditData, setAuditData] = useState({});
    const [isConnected, setIsConnected] = useState(false);
    const [connectionStatus, setConnectionStatus] = useState('Disconnected');
    const [isDeleting, setIsDeleting] = useState(false);
    const [deletingTopic, setDeletingTopic] = useState(null);

    const ws = useRef<WebSocket | null>(null);
    const messagesEndRef = useRef<HTMLDivElement | null>(null);
    const auditIntervalRef = useRef<number | null>(null);

    // WebSocket connection
    useEffect(() => {
        connectWebSocket();
        return () => {
            if (ws.current) {
                ws.current!!.close();
            }
            if (auditIntervalRef.current !== null) {
                clearInterval(auditIntervalRef.current!!);
            }
        };
    }, []);

    const connectWebSocket = () => {
        try {
            ws.current = new WebSocket(WEBSOCKET_URL);

            ws.current!!.onopen = () => {
                setIsConnected(true);
                setConnectionStatus('Connected');
                console.log('WebSocket connected');
            };

            ws.current!!.onmessage = (event) => {
                try {
                    const data = JSON.parse(event.data);
                    const newMessage = {
                        id: Date.now(),
                        type: 'received',
                        content: data.message || event.data,
                        timestamp: new Date(),
                        data: data
                    };
                    setMessages(prev => [...prev, newMessage]);
                } catch (e) {
                    const newMessage = {
                        id: Date.now(),
                        type: 'received',
                        content: event.data,
                        timestamp: new Date()
                    };
                    setMessages(prev => [...prev, newMessage]);
                }
            };

            ws.current!!.onclose = () => {
                setIsConnected(false);
                setConnectionStatus('Disconnected');
                console.log('WebSocket disconnected');
                // Attempt to reconnect after 3 seconds
                setTimeout(connectWebSocket, 3000);
            };

            ws.current!!.onerror = (error) => {
                setConnectionStatus('Error');
                console.error('WebSocket error:', error);
            };
        } catch (error) {
            setConnectionStatus('Error');
            console.error('Failed to connect WebSocket:', error);
        }
    };

    // Auto-scroll to bottom of messages
    useEffect(() => {
        (messagesEndRef.current?.scrollIntoView as any)?.({
            behavior: "smooth",
            block: "end",
        });
    }, [messages]);

    // Fetch audit data every second
    useEffect(() => {
        fetchAllAuditData();
        auditIntervalRef.current = setInterval(fetchAllAuditData, 1000);
        return () => {
            if (auditIntervalRef.current) {
                clearInterval(auditIntervalRef.current!!);
            }
        };
    }, []);

    const fetchAllAuditData = async () => {
        const newAuditData = {};

        await Promise.all(
            AUDIT_ENDPOINTS.map(async (endpoint) => {
                try {
                    const response = await fetch(`${API_BASE}/${endpoint.key}`);
                    if (response.ok) {
                        const data = await response.json();
                        newAuditData[endpoint.key] = Array.isArray(data) ? data : [data];
                    } else {
                        newAuditData[endpoint.key] = [];
                    }
                } catch (error) {
                    console.error(`Error fetching ${endpoint.key}:`, error);
                    newAuditData[endpoint.key] = [];
                }
            })
        );

        setAuditData(newAuditData);
    };

    const sendMessage = () => {
        if (inputMessage.trim() && ws.current && isConnected) {
            const message = {
                id: Date.now(),
                type: 'sent',
                content: inputMessage,
                timestamp: new Date()
            };

            setMessages(prev => [...prev, message]);

            try {
                if (ws.current) {
                    ws.current?.send(JSON.stringify({
                        message: inputMessage,
                        timestamp: new Date().toISOString()
                    }));
                }
            } catch (error) {
                console.error('Error sending message:', error);
            }

            setInputMessage('');
        }
    };

    const handleKeyPress = (e) => {
        if (e.key === 'Enter' && !e.shiftKey) {
            e.preventDefault();
            sendMessage();
        }
    };

    const clearAuditData = async (topicKey) => {
        setDeletingTopic(topicKey);
        try {
            const response = await fetch(`${API_BASE}/${topicKey}`, {
                method: 'DELETE',
                headers: {
                    'Content-Type': 'application/json',
                }
            });

            if (response.ok) {
                // Update local state to reflect the deletion
                setAuditData(prev => ({
                    ...prev,
                    [topicKey]: []
                }));
                console.log(`Successfully cleared ${topicKey}`);
            } else {
                console.error(`Failed to clear ${topicKey}:`, response.statusText);
                // Optionally show an error message to the user
            }
        } catch (error) {
            console.error(`Error clearing ${topicKey}:`, error);
            // Optionally show an error message to the user
        } finally {
            setDeletingTopic(null);
        }
    };

    const clearAllAuditData = async () => {
        setIsDeleting(true);
        try {
            // Call the two specific endpoints for clearing all data
            const deletePromises = [
                fetch('http://localhost:8080/api/audit/recipe-requests/all', {
                    method: 'DELETE',
                    headers: {
                        'Content-Type': 'application/json',
                    }
                }),
                fetch('http://localhost:8080/api/audit/recipe-validations/all', {
                    method: 'DELETE',
                    headers: {
                        'Content-Type': 'application/json',
                    }
                }),
                fetch('http://localhost:8080/api/audit/user-message-receiveds/all', {
                    method: 'DELETE',
                    headers: {
                        'Content-Type': 'application/json',
                    }
                }),
                fetch('http://localhost:8080/api/audit/recipe-postprocessors/all', {
                    method: 'DELETE',
                    headers: {
                        'Content-Type': 'application/json',
                    }
                })
            ];

            const responses = await Promise.all(deletePromises);

            // Check if all requests were successful
            const allSuccessful = responses.every(response => response.ok);

            if (allSuccessful) {
                // Clear all local audit data
                const clearedData = {};
                AUDIT_ENDPOINTS.forEach(endpoint => {
                    clearedData[endpoint.key] = [];
                });
                setAuditData(clearedData);
                console.log('Successfully cleared all audit data');
            } else {
                console.error('Some delete requests failed');
                // Log individual failures
                responses.forEach((response, index) => {
                    if (!response.ok) {
                        const endpoint = index === 0 ? 'recipe-requests' : 'recipe-validation';
                        console.error(`Failed to clear ${endpoint}/all:`, response.statusText);
                    }
                });
            }
        } catch (error) {
            console.error('Error clearing all audit data:', error);
            // Optionally show an error message to the user
        } finally {
            setIsDeleting(false);
        }
    };

    const formatTimestamp = (timestamp) => {
        return new Date(timestamp).toLocaleTimeString();
    };

    const renderChatTab = () => (
        <div className="flex flex-col h-full">
            {/* Connection Status */}
            <div className="flex items-center gap-2 p-3 bg-gray-50 border-b">
                <div className={`w-3 h-3 rounded-full ${isConnected ? 'bg-green-500' : 'bg-red-500'}`}></div>
                <span className="text-sm font-medium">{connectionStatus}</span>
                {!isConnected && (
                    <button
                        onClick={connectWebSocket}
                        className="ml-auto px-3 py-1 text-xs bg-blue-500 text-white rounded hover:bg-blue-600"
                    >
                        Reconnect
                    </button>
                )}
            </div>

            {/* Messages */}
            <div className="flex-1 overflow-y-auto p-4 space-y-3">
                {messages.map((message) => (
                    <div
                        key={message.id}
                        className={`flex ${message.type === 'sent' ? 'justify-end' : 'justify-start'}`}
                    >
                        <div
                            className={`max-w-xs lg:max-w-md px-4 py-2 rounded-lg ${
                                message.type === 'sent'
                                    ? 'bg-blue-500 text-white'
                                    : 'bg-gray-200 text-gray-900'
                            }`}
                        >
                            <div className="text-sm">{message.content}</div>
                            <div className={`text-xs mt-1 opacity-75`}>
                                {formatTimestamp(message.timestamp)}
                            </div>
                        </div>
                    </div>
                ))}
                <div ref={messagesEndRef as RefObject<HTMLDivElement>} />
            </div>

            {/* Input */}
            <div className="p-4 border-t">
                <div className="flex gap-2">
          <textarea
              value={inputMessage}
              onChange={(e) => setInputMessage(e.target.value)}
              onKeyPress={handleKeyPress}
              placeholder="Type your message..."
              className="flex-1 px-3 py-2 border border-gray-300 rounded-lg resize-none focus:outline-none focus:ring-2 focus:ring-blue-500"
              rows="1"
              disabled={!isConnected}
          />
                    <button
                        onClick={sendMessage}
                        disabled={!isConnected || !inputMessage.trim()}
                        className="px-4 py-2 bg-blue-500 text-white rounded-lg hover:bg-blue-600 disabled:opacity-50 disabled:cursor-not-allowed"
                    >
                        Send
                    </button>
                </div>
            </div>
        </div>
    );

    const renderAuditTab = () => (
        <div className="p-4 h-full overflow-y-auto">
            {/* Clear All Button */}
            <div className="mb-4 flex justify-between items-center">
                <h2 className="text-lg font-semibold">Audit Data Monitor</h2>
                <button
                    onClick={clearAllAuditData}
                    disabled={isDeleting}
                    className="flex items-center gap-2 px-4 py-2 bg-red-500 text-white rounded-lg hover:bg-red-600 disabled:opacity-50 disabled:cursor-not-allowed"
                >
                    {isDeleting ? (
                        <RefreshCw className="w-4 h-4 animate-spin" />
                    ) : (
                        <Trash2 className="w-4 h-4" />
                    )}
                    {isDeleting ? 'Clearing...' : 'Clear All'}
                </button>
            </div>

            {/* Audit Boxes Grid */}
            <div className="grid grid-cols-4 gap-4">
                {AUDIT_ENDPOINTS.map((endpoint, index) => (
                    <div
                        key={endpoint.key}
                        className={`border rounded-lg p-4 ${endpoint.color} ${
                            index === AUDIT_ENDPOINTS.length - 1 ? 'col-span-4' :
                                index % 3 === 0 ? 'col-span-2' : 'col-span-1'
                        }`}
                    >
                        <div className="flex justify-between items-center mb-3">
                            <h3 className="font-medium text-sm">{endpoint.label}</h3>
                            <button
                                onClick={() => clearAuditData(endpoint.key)}
                                disabled={deletingTopic === endpoint.key}
                                className="p-1 text-gray-500 hover:text-red-500 disabled:opacity-50 disabled:cursor-not-allowed"
                                title="Clear this data"
                            >
                                {deletingTopic === endpoint.key ? (
                                    <RefreshCw className="w-4 h-4 animate-spin" />
                                ) : (
                                    <Trash2 className="w-4 h-4" />
                                )}
                            </button>
                        </div>

                        <div className="text-xs text-gray-600 mb-2">
                            Count: {auditData[endpoint.key]?.length || 0}
                        </div>

                        <div className="max-h-40 overflow-y-auto space-y-2">
                            {auditData[endpoint.key]?.length > 0 ? (
                                auditData[endpoint.key].map((item, index) => (
                                    <div
                                        key={index}
                                        className="bg-white/50 p-2 rounded text-xs border"
                                    >
                    <pre className="whitespace-pre-wrap text-xs text-left">
                      {typeof item === 'object' ? JSON.stringify(item, null, 2) : item}
                    </pre>
                                    </div>
                                ))
                            ) : (
                                <div className="text-gray-500 text-xs italic">No data</div>
                            )}
                        </div>
                    </div>
                ))}
            </div>
        </div>
    );

    return (
        <div className="w-full h-screen bg-white flex flex-col">
            {/* Header */}
            <div className="bg-gray-900 text-white p-4">
                <h1 className="text-xl font-bold">Kafka Audit & Chat Interface</h1>
            </div>

            {/* Tabs */}
            <div className="flex border-b">
                <button
                    onClick={() => setActiveTab('chat')}
                    className={`flex items-center gap-2 px-6 py-3 font-medium border-b-2 transition-colors ${
                        activeTab === 'chat'
                            ? 'border-blue-500 text-blue-600 bg-blue-50'
                            : 'border-transparent text-gray-600 hover:text-gray-900'
                    }`}
                >
                    <MessageCircle className="w-4 h-4" />
                    Chat
                </button>
                <button
                    onClick={() => setActiveTab('audit')}
                    className={`flex items-center gap-2 px-6 py-3 font-medium border-b-2 transition-colors ${
                        activeTab === 'audit'
                            ? 'border-blue-500 text-blue-600 bg-blue-50'
                            : 'border-transparent text-gray-600 hover:text-gray-900'
                    }`}
                >
                    <Activity className="w-4 h-4" />
                    Audit Monitor
                </button>
            </div>

            {/* Tab Content */}
            <div className="flex-1 overflow-hidden">
                {activeTab === 'chat' ? renderChatTab() : renderAuditTab()}
            </div>
        </div>
    );
};

export default KafkaAuditUI;