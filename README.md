# Langtools

## About the project

This project is complementary to [ir](https://github.com/dashluu/ir). It serves as a user interface for the
compiler at the backend. This helps me debug the compiler and can be considered as a simple code editor, although that
is
not the intention. In summary, this is just another hobby project to test how well the compiler works. Currently, this
project can only generate intermediate representation(IR) without executing any code or running any virtual machine.

## Architecture

### Frontend and backend

The frontend and backend are separated, so they do not have knowledge of one another. This might cause some problems
such as the server not knowing if a domain is to be trusted. Hence, I enabled Cross-Origin Resource Sharing(CORS) by
listing the port where React is deployed, which is usually port 3000 on the local host. In the future, I might look
deeper into this problem, but for now, it is there for demo purposes.

* **Frontend**:
    * React framework.
    * Single-page application.
    * Modular components:
        * **Editor**: a component for editing the source code.
        * **OutputView**: a component for viewing the output after the code is parsed.
        * **App**: includes `Editor` and `OutputView` as a single modular component.
* **Backend**:
    * Spring Boot framework.
    * Uses the MVC design pattern.
    * Receives a string representing the user code and generates IR using
      [ir](https://github.com/dashluu/ir).
    * Sends the result as a JSON array containing the IR instructions.

## Demo

![](demo_gif.gif)
