
import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;

public class Main {

    public static void main(String[] args) {

        try {
                LocateRegistry.createRegistry(5010);
                INameNode nameNode = new NameNode();
                Naming.rebind("rmi://localhost:5010/namenode", nameNode);
                System.out.println("DFS service is running.");

        } catch (Exception e) {
            System.out.println("Trouble: " + e);
        }

    }
}
