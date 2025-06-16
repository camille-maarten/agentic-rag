# Red Hat AI Inference Server Examples

This repo provides a very simple example of how to get Red Hat AI Inference Server running on OpenShift.

You will have to update the pull-secret and hf-secret files with your Red Hat pull secret and Hugging Face token in order for this to work.

To deploy simple enter the command:
```
oc apply -k ./base/
```
