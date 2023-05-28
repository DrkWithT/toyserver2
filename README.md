# README
## Project: Toy Server 2
## Derek Tan

### Summary
This repository is for a self-made, _toy_ HTTP(S)/1.1 server using Java SE 18 libraries. Within this project, the server will implement a working part of the HTTP/1.1 specification in _RFC 9112_ based on a To-Do list. This also means that not all features of HTTP/1.1 will be implemented for the sake of simplicity.

### Usage:
 1. Clone this repository.
 2. Ensure that Java SE 18 and Maven are installed on your system.
 3. Create a `secrets` folder with self-signed certificates named:
    - `mykeys.key`, `mycert.crt`
 4. Run the project.
 5. Go to `https://localhost:5000`.

### Implenentation To-Dos:
 1. Create the main server class and its worker runnables.
    - `AcceptorWorker`: This Runnable class models a helper task that listens, accepts, and spawns HTTP/1.1 connection workers.
 2. Create utility enums and classes for constants, requests, and responses.
 3. Create static resource cache.
 4. Integrate utilities and cache into main worker code.
 5. Test.
