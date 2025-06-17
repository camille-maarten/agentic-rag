import './App.css'
import {Route, Routes} from "react-router";
import KafkaAuditUI from "./chat-ui.tsx";

function App() {
    return (
        <Routes>
            <Route index element={<KafkaAuditUI/>}/>
        </Routes>
    )
}

export default App
