import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IDataNode extends Remote {

    boolean ifExist(String fileName) throws RemoteException;

    boolean hasLatestVersion(String fileName) throws RemoteException;

    String read(String fileName) throws RemoteException;

    String write(String fileName, String data) throws RemoteException;

    String getResponse(String req) throws RemoteException;


}
