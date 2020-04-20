import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

public class NameNode extends UnicastRemoteObject implements INameNode {
    // port -> datanode
    private Map<Integer, IDataNode> dataNodes;
    // filename -> at most 3 datenode_ports who have read the file recently.
    private Map<String, Queue<Integer>> fileToDataNode;
    //filename -> the most recent datanode_port
    private Map<String, Integer> latestDatanode;
    //filename -> mode("WRITE" or "CLOSE")
    private Map<String, String> mode;
    //filrname -> latest version
    private Map<String, String> version;
    private final static int ReplicaNumber = 3;
    private final static int[] Ports = new int[]{1900, 2000, 2100, 2200, 2300};

    public NameNode() throws RemoteException {
        this.fileToDataNode = new HashMap<>();
        this.latestDatanode = new HashMap<>();
        this.mode = new HashMap<>();
        this.version = new HashMap<>();
        this.dataNodes = new HashMap<>();


        try {
            for (int port : Ports) {
                LocateRegistry.createRegistry(port);
                IDataNode datanode = new DataNode(port);
                Naming.rebind("rmi://localhost:" + port + "/datanode", datanode);
                this.dataNodes.put(port, datanode);
            }

        } catch (Exception e) {
            System.out.println("Trouble in NameNode: " + e);
        }
    }

    private void updateFileStatus(String fileName, String mode) {
        this.mode.put(fileName, mode);
    }

    @Override
    public boolean ifExist(String fileName) throws RemoteException {
        return this.latestDatanode.containsKey(fileName);

    }

    //TODO
    @Override
    public byte[] openFile(String fileName, int dn_port) throws RemoteException {

        int owner_port;

        if (!this.latestDatanode.containsKey(fileName)
                || this.fileToDataNode.get(fileName) == null
                || this.fileToDataNode.get(fileName).size() == 0) {
            return new byte[0];
        } else {

            owner_port = this.latestDatanode.get(fileName);

//TODO    set a timeout: if the datanode doesn't response in 10 seconds,
//        get another datanode who has the file (but may be not int he latest version)

//            long endTimeMillis = System.currentTimeMillis() + 10000;
//            while (true) {
//                //get the datanode_id that keeps the latest file.
//                owner_port = this.latestDatanode.get(fileName);

//                if (System.currentTimeMillis() > endTimeMillis) {
//                    owner_port = this.fileToDataNode.get(fileName).peek();
//                }

            IDataNode owner = this.dataNodes.get(owner_port);

            // add the most recent datanode into the queue that contains at most 3 datanodes that keep the file.
            if (!this.fileToDataNode.containsKey(fileName)) {
                this.fileToDataNode.put(fileName, new LinkedList<>());
            }
            Queue<Integer> keepers = this.fileToDataNode.get(fileName);
            keepers.offer(dn_port);
            if (keepers.size() > ReplicaNumber) {
                keepers.poll();
            }

            return owner.read(fileName).getBytes();

        }

    }

    @Override
    public void updateMetadata(String fileName, int dn_port, String newVersion) throws RemoteException {
        // update the filename -> datanode that modified it most recently.
        this.latestDatanode.put(fileName, dn_port);
        // add the most recent datanode into the queue that contains at most 3 datanodes that keep the file.
        if (!this.fileToDataNode.containsKey(fileName)) {
            this.fileToDataNode.put(fileName, new LinkedList<>());
        }
        Queue<Integer> keepers = this.fileToDataNode.get(fileName);
        keepers.offer(dn_port);
        if (keepers.size() > ReplicaNumber) {
            keepers.poll();
        }
        this.version.put(fileName, newVersion);
        System.out.println("fileToDataNode: " + this.fileToDataNode);
        System.out.println("latestDatanode: " + this.latestDatanode);
        System.out.println("version: " + this.version);

    }

//TODO
    @Override
    public byte[] closeFile(String fileName) throws RemoteException {
        return new byte[0];
    }

    @Override
    public String getVersion(String fileName) throws RemoteException {
        return this.version.get(fileName);
    }

    @Override
    public String getMode(String fileName) throws RemoteException {
        return this.mode.get(fileName);
    }

    @Override
    public void lockFile(String fileName) throws RemoteException {
        this.mode.put(fileName, FileMode.WRITE);
    }

    @Override
    public void releaseFile(String fileName) throws RemoteException {
        this.mode.put(fileName, FileMode.CLOSE);
    }


    @Override
    public void exit() throws RemoteException {
        try{

            // Unexport; this will also remove us from the RMI runtime
            UnicastRemoteObject.unexportObject(this, true);

            System.out.println("NameNode exiting.");
        }
        catch(Exception e){

            System.out.println("Server exiting Error.");
        }
    }


}
