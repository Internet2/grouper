---
title: "Grouper AI with local ollama LLM"
space: Grouper
pageId: 28548844
version: 2
lastUpdated: 2025-12-17T20:21:48.232Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548844/Grouper+AI+with+local+ollama+LLM
---

[Ollama](https://ollama.com/) is a local LLM hosting and running platform that can download and execute large language models using a simple, container-like model format. It provides a unified CLI and API for running, managing, and customizing models with minimal setup, leveraging GPU acceleration when available.

[Open WebUI](https://openwebui.com/) is a lightweight, self-hosted web interface that provides a chat-style front end for interacting with an Ollama back end. Through it, you can manage models and create derived models with custom prompts, context, and knowledge documents.

# Installation

While you can install ollama locally, it is easier to use the pre-made Docker containers. Here is an example of a docker-compose using both ollama and open-webui.

```
services:
  ollama:
    image: ollama/ollama:0.12.1
    ports:
      - "11434:11434"
    volumes:
      - ./ollama/.ollama:/root/.ollama
    runtime: nvidia
    environment:
      - NVIDIA_VISIBLE_DEVICES=all

  open-webui:
    image: ghcr.io/open-webui/open-webui:main
    ports:
      - "8080:8080"
    environment:
      - OLLAMA_BASE_URL=http://ollama:11434
      - NVIDIA_VISIBLE_DEVICES=all
      - OPENAI_API_KEY=supersecret
    volumes:
      - ./open-webui/data:/app/backend/data
    depends_on:
      - ollama
    runtime: nvidia
```

Note that in this example, if you have a GPU capable of running AI workloads, you can tell Docker to utilize it.

# Downloading and installing models

The Ollama [model listing](https://ollama.com/library?sort=popular) shows the models available for download. When choosing a model, you will want to choose models small enough to be able to run on your system. Each model name may have multiple model versions with different sizes, context windows, and capabilities. The model size is approximately how much RAM it will need when it runs. If you are using the GPU for computation, choose the size of your GPU and not your host RAM.

Once you have the exact model name, use the ollama command line to download and install it. For example, with the ollama Docker container running, for llama3.2:3b:

```
docker-compose exec ollama bash
ollama pull llama3.2:3b
ollama list
```

You can install multiple models to Ollama and choose between them.

# Using Open WebUI

If using the open-webui Docker container, once it's started you can reach the main page at [http://localhost:8080/.](http://localhost:8080/.) Click on Get Started, it will ask for an email and password for the administrator account.

Note the downloaded model(s) appear in the Model dropdown.

# Creating a custom Grouper model

In Open WebUI, go to Workspace > Models > New Model. Create a name and choose the base model. The system prompt is where you can specify the model’s global behavior, role, and constraints before any user input is processed. It is used to enforce consistent instructions such as tone, domain expertise, formatting rules, and guardrails across all conversations with that model. For knowledge, you can upload local files from the browser (i.e., they they don't need to be in the Docker containers), or you can manage knowledge stacks through the Workspace Knowledge tab.

Setting up tools is confusing, and it's not clear the best way to do it. It appears you can add them through Workspace Tools, but I didn't have luck with that. I was able to add tools via Profile (lower left) > Admin Panel > Settings > External Tools. Even with this setup, chats don't always use the tools that are available. This may be module-specific.
