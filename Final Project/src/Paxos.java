import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public class Paxos {

  private String proposal;
  private Proposer proposer;
  private List<Acceptor> acceptors = new ArrayList<>();
  private static int MaxProposal = 100;
//  static int[] ports = new int[]{1985, 1986, 1987, 1988, 1989};
  private final static int[] Ports = new int[]{1900, 2000, 2100, 2200, 2300};
  private int maxKeyPort = 0;
  private Integer maxKey = 0;
  private INameNode nd;
  private Queue<Integer> dataNodes;


  public Paxos(Proposer proposer, String proposal) throws RemoteException {
    this.proposer = proposer;
    this.proposal = proposal;
    this.acceptors = checkAcceptors();
  }

  private List<Acceptor> checkAcceptors() throws RemoteException {
    List<Acceptor> accpetors = new ArrayList<>();
    try {
      this.nd = (INameNode) Naming.lookup("rmi://localhost:5010/namenode");
      Map<String, Queue<Integer>> fileToDataNode = this.nd.getFileToDataNode();
      String[] request = this.proposal.split("\\s+");
//      // get the data nodes which contain the file
//      this.dataNodes = fileToDataNode.get(request[1]);
//      // get all the acceptors from the data nodes.
//      for (Integer id:dataNodes) {
//          String stub = "rmi://localhost:"+id+"/namenode";
//          IDataNode dataNode = (IDataNode) Naming.lookup(stub);
//          this.acceptors.add(dataNode.getAcceptor());
//      }

      // test purpose: check all the data node acceptors
      for (Integer id:Ports) {
        String stub = "rmi://localhost:"+id+"/datanode";
        IDataNode dataNode = (IDataNode) Naming.lookup(stub);
        accpetors.add(dataNode.getAcceptor());
      }
    }
    catch (MalformedURLException murle) {
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
    return accpetors;
  }


//  public String commit() {
//    String output = "No content.";
//    String[] request = this.proposal.split("\\s+");
//    // get the proposal dataNode
//    Integer dataNodeId = this.proposer.getPort();
//    try {
//      String stub = "rmi://localhost:"+dataNodeId+"/datanode";
//      IDataNode dataNode = (IDataNode) Naming.lookup(stub);
//      if (request[0].equals(FileMode.READ)) {
//        return dataNode.read(request[1]);
//      } else if (request[0].equals(FileMode.WRITE)) {
//
//        return dataNode.write(request[1], request[2]);
//      }
//    }
//    catch (MalformedURLException murle) {
//      System.out.println();
//      System.out.println(
//          "MalformedURLException");
//      System.out.println("Invalid port number, please restart with a valid port number.");
//    }
//    catch (RemoteException re) {
//      System.out.println();
//      System.out.println(
//          "RemoteException");
//      System.out.println("The nameNode is down!");
//    }
//    catch (NotBoundException nbe) {
//      System.out.println();
//      System.out.println(
//          "NotBoundException");
//      System.out.println("The nameNode on this port number is down! please try another port.");
//    }
//
//    return output;
//  }

  public boolean run() {
    if (acceptors.size()== 0) {
      return false;
    }
    int quorum = Math.floorDiv(acceptors.size(), 2) + 1;
    Prepare prepare = Prepare.nextPrepare(proposal);
    int count = 0;
    while (true) {
      // if reach the max number of proposal number, break and return to client, request failed.
      if (count == MaxProposal) {
        System.out.println("Propoased "+MaxProposal+" times. Quit.");
        return false;
      }
      // do the prepares and get the promises
      System.out.println("proposer:" + proposer + " count:" + count + " prepare:" + prepare);
      List<Promise> promises = new ArrayList<>();
      for (int i = 0; i < acceptors.size(); i++) {
        Acceptor acceptor = acceptors.get(i);
        Promise p = Acceptor.doPromise(acceptor, prepare);
        if(p.isStatus()) {
          promises.add(p);
        }
      }
//      promises = promises.stream().sorted(Comparator.comparing(Promise::getKey, Comparator.nullsFirst(Integer::compareTo))).collect(Collectors.toList());
      // if the prepare get rejected, propose another one.
      if(promises.size()<quorum) {
        Promise promise = new Promise(true, null, null);
        if(promises.size()!=0)
          promise = promises.get(promises.size()-1);
        prepare = Prepare.nextPrepare(promise.getValue());
        continue;
      }

      // proposers get promises from quorum and does accept to all Acceptors.
      Accept accept = new Accept(prepare.getKey(), prepare.getValue());
      List<Announce> announces = new ArrayList<>();
      for (int i = 0; i < acceptors.size(); i++) {
        Acceptor acceptor = acceptors.get(i);
        Announce r = Acceptor.doAnnounce(acceptor, accept);
        if(r.isStatus()) {
          announces.add(r);
        }
      }

      // if not all acceptors decides to announce the value. propose another one.
      if(announces.size()<quorum) {
        prepare = Prepare.nextPrepare(accept.getValue());
        continue;
      }

      break;
    }
    System.out.println("select "+prepare);
    return true;
  }
}