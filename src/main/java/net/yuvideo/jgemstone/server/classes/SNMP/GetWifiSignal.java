package net.yuvideo.jgemstone.server.classes.SNMP;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import net.yuvideo.jgemstone.server.classes.database;
import org.snmp4j.CommunityTarget;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.GenericAddress;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.snmp4j.util.DefaultPDUFactory;
import org.snmp4j.util.TreeEvent;
import org.snmp4j.util.TreeUtils;

public class GetWifiSignal {

  private database db;
  private ArrayList<SNMPDevices> snmpDevicesArrayList = new ArrayList<>();
  private boolean error;
  private String errorMSG;


  public GetWifiSignal() {

  }


  public ArrayList<SNMPDevices> getMACDevices(String targetHost, String comunity,
      String snmpVersion) {
    String oidWalk = ".1.3.6.1.4.1.14988.1.1.1.2.1.3";
    CommunityTarget target = new CommunityTarget();
    target.setAddress(GenericAddress.parse(String.format("udp:%s/161", targetHost)));
    target.setCommunity(new OctetString(comunity));
    target.setRetries(2);
    target.setTimeout(1500);
    target.setVersion(SnmpConstants.version1);
    Map<String, String> result = doWalk(oidWalk, target);

    return snmpDevicesArrayList;
  }

  private Map<String, String> doWalk(String oidWalk, CommunityTarget target) {
    Map<String, String> result = new TreeMap<>();
    try {
      TransportMapping<? extends Address> transportMapping = new DefaultUdpTransportMapping();
      Snmp snmp = new Snmp(transportMapping);
      snmp.listen();

      TreeUtils treeUtils = new TreeUtils(snmp, new DefaultPDUFactory());
      List<TreeEvent> events = treeUtils.getSubtree(target, new OID(oidWalk));
      if (events == null || events.size() == 0) {
        System.out.println("ERROR");
        setError(true);
        setErrorMSG("Ne mogu da procitam tablu");
        return result;
      }

      for (TreeEvent event : events) {
        if (event == null) {
          continue;
        }

        if (event.isError()) {
          System.out.println(event.getErrorMessage());
          setErrorMSG(event.getErrorMessage());
          setError(true);
        }

        VariableBinding[] variableBindings = event.getVariableBindings();
        if (variableBindings == null || variableBindings.length == 0) {
          continue;
        }

        for (VariableBinding variableBinding : variableBindings) {
          if (variableBinding == null) {
            continue;
          }
          System.out.println(
              "." + variableBinding.getOid().toString() + " " + variableBinding.getVariable()
                  .toString());
        }
      }
      snmp.close();
    } catch (IOException e) {
      e.printStackTrace();
    }

    return result;

  }


  public boolean isError() {

    return error;
  }

  public void setError(boolean error) {
    this.error = error;
  }

  public String getErrorMSG() {
    return errorMSG;
  }

  public void setErrorMSG(String errorMSG) {
    this.errorMSG = errorMSG;
  }
}
