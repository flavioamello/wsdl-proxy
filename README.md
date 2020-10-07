## Wsdl proxy

Simple java project to handle remote wsdl.
If you are trying to read a wsdl using a proxy to a vpn, or using a tunnel, wsdl references will not be found by you soap client like SoapUi for example, the client will try to read a referenced xml on an unaccessible host.

The purpose of this project is to create a "proxy" where it will replace schemaLocation and location attributes to this proxy on your localhost, so you soap client will be able to read all referenced xml files.

## How to use
Clone this project, edit application.properties with your wsdl host (proxy url or tunnel at localhost:port) and run `mvn spring-boot:run`.
Point your soap client to `http://localhost:8080/<wsdl-path-and-params>` as the wsdl location and generate your client.

### Side effect
As a side effect, your soap client will create requests pointing to `http://localhost:8080/<path-of-services>`.
This project also has a endpoint where it will receive post requests, forward it to your proxy/tunnel address and return the response, so you don't need to chenge your soap client address when running this application.

## Contact
Feel free to contact me at flavioa.mello@gmail.com