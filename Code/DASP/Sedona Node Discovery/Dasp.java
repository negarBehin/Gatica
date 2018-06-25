import sedona.dasp.DaspSocket;
import sedona.dasp.DiscoveredNode;

public class Dasp
{
  // Default Sedona port
  private int port = 1876;
  
  public void discoveredNodes() throws Exception{
    
    // TODO  set Sedona home correctly if applicable
    System.setProperty("sedona.home" , " /usr/src/sedona/");
    
    DaspSocket daspSocket = DaspSocket.open(-1 , null , DaspSocket.SOCKET_QUEUING);
    daspSocket.discover(this.port, false);
    Thread.sleep(100); 
    DiscoveredNode[] discoverd = daspSocket.getDiscovered();
    for (DiscoveredNode discoveredNode : discoverd)
    {
      System.out.println("Discovered SVM IP/Port: " + discoveredNode.addr().getHostAddress()+":"+this.port);
      System.out.println("Discovered SVM Platform ID: " + discoveredNode.platformId());
    }
  }  
  
  public int getPort()
  {
    return port;
  }

  public void setPort(int port)
  {
    this.port = port;
  }


  public static void main(String[] args)
  {    
    Dasp dasp = new Dasp();   
    try
    {
      dasp.discoveredNodes();
    }
    catch (Exception e)
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

}
