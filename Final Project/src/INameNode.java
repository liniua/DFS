import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Queue;
import java.util.Map;


public interface INameNode extends Remote {

    boolean ifExist(String fileName) throws RemoteException;

    /* OpenFileResponse openFile(OpenFileRequest) */
    /* Method to open a file given file name with read-write flag*/
    byte[] openFile(String fileName, int dn_port) throws RemoteException;

    void updateMetadata(String fileName, int dn_port, String newVersion) throws RemoteException;

    /* CloseFileResponse closeFile(CloseFileRequest) */
    byte[] closeFile(String fileName) throws RemoteException;

    String getVersion(String fileName) throws RemoteException;


    String getMode(String fileName) throws RemoteException;

    void lockFile(String fileName) throws RemoteException;

    void releaseFile(String fileName) throws RemoteException;

    void exit() throws RemoteException;

    Map<String, Queue<Integer>> getFileToDataNode() throws RemoteException;

}
