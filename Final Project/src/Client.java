// Java implementation for a client

import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Scanner;

// Client class
public class Client {
    private final static String GREETING = "Please type an operation in following format: \n[READ FILENAME]\n[WRITE FILENAME CONTENT]\n[exit]";
    private Scanner scanner = null;


    public Client() {

        try {
            // look up the remote object
            System.out.println("Please pick a port number: {1900, 2000, 2100, 2200, 2300}");
            scanner = new Scanner(System.in);
            String port = scanner.nextLine();
            IDataNode dataNode = (IDataNode) Naming.lookup("rmi://localhost:" + port + "/datanode");

            System.out.println("Connected");


            while (true) {
                System.out.println(GREETING);
                String request = scanner.nextLine();

                while (!isValid(request)) {
                    System.out.println("Invalid request.");
                    System.out.println(GREETING);
                    request = scanner.nextLine();
                }

                if(request.equals("exit"))
                {
                    System.out.println("Disconnected to service dataNode.");
                    break;
                }

                System.out.println("Request: " + request);

                String received = dataNode.getResponse(request);
                System.out.println("Received: " + received);
            }

            // closing scanner
            scanner.close();
            System.out.println("Closing connection");

        } catch (MalformedURLException murle) {
            System.out.println();
            System.out.println(
                    "MalformedURLException");
            System.out.println("Invalid port number, please restart with a valid port number.");
        }
        catch (RemoteException re) {
            System.out.println();
            System.out.println(
                    "RemoteException");
            System.out.println("The server is down!");
        }
        catch (NotBoundException nbe) {
            System.out.println();
            System.out.println(
                    "NotBoundException");
            System.out.println("The server on thi sport number is down! please try another port.");
        }
        catch (ArithmeticException ae) {
            System.out.println();
            System.out.println(
                    "java.lang.ArithmeticException");
            System.out.println(ae);
        }


    }

    private boolean isValid(String request) {

        String[] req = request.split("\\s", 3);

        return (req.length == 1 && req[0].equals("exit"))
                || (req.length == 2 && req[0].equals("READ"))
                || (req.length == 3 && req[0].equals("WRITE"));
    }



    public static void main(String[] args) throws IOException {

        Client client = new Client();



    }
}


