
import java.io.*;
import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;

public class Main {

    public static void main(String[] args) {

        try {
                LocateRegistry.createRegistry(5010);
                INameNode nameNode = new NameNode();
                Naming.rebind("rmi://localhost:5010/namenode", nameNode);

        } catch (Exception e) {
            System.out.println("Trouble: " + e);
        }
//        String fileName = "Test";
//        String content = "llsdsdasf";
//        byte[] read_buffer = content.getBytes();
//
//        try {
//            File newFile = new File(fileName);
//            if (newFile.createNewFile()) {
//                System.out.println("File created: " + newFile.getName());
//            }
//        } catch (IOException e) {
//            System.out.println("An error occurred.");
//            e.printStackTrace();
//        }
//        try {
//            BufferedWriter files_obj = new BufferedWriter(
//                    new FileWriter(fileName, true));
//            files_obj.write(content);
//            files_obj.write("\n");
//            files_obj.close();
////            FileOutputStream data_file_obj = new FileOutputStream(fileName);
////            data_file_obj.write(read_buffer);
////            data_file_obj.close();
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }
}
