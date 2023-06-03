# README
## Project: Toy Server 2
## Derek Tan

### Summary
This repository is for a self-made, _toy_ HTTP/1.1 server using Java SE 18 libraries. Within this project, the server will implement a working part of the HTTP/1.1 specification in _RFC 9112_ based on a To-Do list. This also means that not all features of HTTP/1.1 will be implemented for the sake of simplicity.

### Usage:
 1. Clone this repository.
 2. Ensure that Java SE 18 and Maven are installed on your system.
 3. Run the project.
 4. Go to `http://localhost:8000`.

### Some Features
 1. Crude server route handler support using `IRequestHandler` lambda expressions. Each one takes a static resource cache as context along with request, response, and an optional HTTP error status. These mock a low-level API where calls to write a response status line, headers, and body are explicit. These should be explicity bound as needed.
 2. Fallback server route handler to serve HTTP errors such as `404` or `500`. This must be explicity bound before server start.
 3. Crude finite state machine patterns in `ServerWorker` and `HttpReader` to handle simple HTTP responses in an orderly way.
 4. Supports a simple static folder path, but only uses the 1st nested level of files.
