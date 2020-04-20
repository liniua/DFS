import java.io.*;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataNode extends UnicastRemoteObject implements IDataNode {

    public int port;
    public List<String> files;
    public Map<String, String> version;
    private Acceptor acceptor;

    private INameNode nd;

    public DataNode(int port) throws RemoteException, IOException {
        this.port = port;
        this.version = new HashMap<>();
        this.files = new ArrayList<>();

        this.acceptor = new Acceptor(port);


        File f = new File("filesInDataNode" + this.port);
        if (!f.exists()) {
            f.createNewFile();
        }

        //Setting up the files
        BufferedReader br = null;
        br = new BufferedReader(new FileReader(f));
        String line;
        while((line = br.readLine()) != null) {
            this.files.add(line);
        }
        br.close();
    }

    @Override
    public Acceptor getAcceptor() {
        // if the acceptor failed, generate a new acceptor
        if (acceptor.failure()) {
            this.acceptor = new Acceptor(this.port);
        }
        return this.acceptor;
    }

    @Override
    public boolean ifExist(String fileName) throws RemoteException {

        return this.files.contains(fileName);
    }

    @Override
    public boolean hasLatestVersion(String fileName) throws RemoteException {
        if (this.nd == null) {
            try {
                this.nd = (INameNode) Naming.lookup("rmi://localhost:5010/namenode");

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
                System.out.println("The nameNode is down!");
            }
            catch (NotBoundException nbe) {
                System.out.println();
                System.out.println(
                        "NotBoundException");
                System.out.println("The nameNode on this port number is down! please try another port.");
            }
            catch (ArithmeticException ae) {
                System.out.println();
                System.out.println(
                        "java.lang.ArithmeticException");
                System.out.println(ae);
            }
        }
        return this.nd.getVersion(fileName).equals(this.version.get(fileName));
    }

    @Override
    public String read(String fileName) throws RemoteException {

        // The file is in the local disk with the latest version.
        if (ifExist(fileName) && hasLatestVersion(fileName)){
            File f1 = new File(fileName);
            FileInputStream f = null;
            try {
                f = new FileInputStream(f1);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            int size = 32*1024*1024;
            byte[] read_buffer= new byte[size];
            BufferedInputStream bis = new BufferedInputStream(f);
            int tmp = 0;
            try {
                tmp = bis.read(read_buffer);
            } catch (IOException e) {
                e.printStackTrace();
            }

            byte[] content = new byte[tmp];
            System.arraycopy(read_buffer, 0, content, 0, tmp);

            return new String(content);

        } else { // The file is not in the local disk OR the file in the local disk is old version.

            byte[] read_buffer = this.nd.openFile(fileName, this.port);
            String content = new String(read_buffer);

            //write to local disk
            this.files.add(fileName);
            this.version.put(fileName, nd.getVersion(fileName));
            //Write to File
            writeToLocal(fileName, content);

            return content;

        }

    }


    @Override
    public String write(String fileName, String data) throws RemoteException {
        if (this.nd == null) {
            try {
                this.nd = (INameNode) Naming.lookup("rmi://localhost:5010/namenode");

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
                System.out.println("The nameNode is down!");
            }
            catch (NotBoundException nbe) {
                System.out.println();
                System.out.println(
                        "NotBoundException");
                System.out.println("The nameNode on this port number is down! please try another port.");
            }
            catch (ArithmeticException ae) {
                System.out.println();
                System.out.println(
                        "java.lang.ArithmeticException");
                System.out.println(ae);
            }
        }

        // The file already exists.
        if (this.nd.ifExist(fileName)) {

            if (this.nd.getMode(fileName).equals(FileMode.WRITE)) {
                return "Cannot write! Another user is trying to write in this file.";
            }
            // pull the latest version to local disk
            read(fileName);
        }

        System.out.println("Start to write");
        //write to local disk
        this.nd.lockFile(fileName);
        System.out.println("file's mode: " + this.nd.getMode(fileName));
        writeToLocal(fileName, data);
        this.nd.releaseFile(fileName);

        //update metadata
        String newVersion = new Timestamp(System.currentTimeMillis()).toString();
        this.version.put(fileName, newVersion);
        this.nd.updateMetadata(fileName, this.port, newVersion);
        return "Write Successfully";
    }

    @Override
    public String getResponse(String req) throws RemoteException {

        String output = "No content.";
        Proposer proposer = new Proposer(this.port);
        Paxos paxos = new Paxos(proposer, req);
        boolean paxosResult = paxos.run();
        if (!paxosResult) {
            return "Commit failed.";
        }
//        output = paxos.commit();
//        return output;

        String[] request = req.split("\\s", 3);
        if (request[0].equals(FileMode.READ)) {

            return read(request[1]);
        } else if (request[0].equals(FileMode.WRITE)) {

            return write(request[1], request[2]);
        }
        return "No content.";
    }


    private void writeToLocal(String fileName, String data) throws RemoteException {
        //Write to File
        try {
            File newFile = new File(fileName);
            if (newFile.createNewFile()) {
                this.files.add(fileName);
                System.out.println("files in local : " + this.files);
                // add the new filename into the filesInDataNode
                // Open given file in append mode.
                BufferedWriter files_obj = new BufferedWriter(
                        new FileWriter("filesInDataNode" + this.port, true));
                files_obj.write(fileName);
                files_obj.write("\n");
                files_obj.close();

                System.out.println("File created: " + newFile.getName());
            }
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }

        try {
            BufferedWriter data_file_obj = new BufferedWriter(new FileWriter(fileName, true));
            data_file_obj.write(data);
            data_file_obj.write("\n");
            data_file_obj.close();


        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
