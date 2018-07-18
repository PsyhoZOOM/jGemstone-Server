package net.yuvideo.jgemstone.server.classes.SNMP;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import net.yuvideo.jgemstone.server.classes.database;
import org.apache.log4j.Logger;
import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.GenericAddress;
import org.snmp4j.smi.Integer32;
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
  private Logger LOGGER = Logger.getLogger("WIFI_SIGNAL");


  public GetWifiSignal() {

  }


  public ArrayList<SNMPDevices> getMACDevices(String targetHOST, String targetIP, String comunity,
      String snmpVersion) {
    String oidWalk = ".1.3.6.1.4.1.41112.1.4.7.1.1";
    CommunityTarget target = new CommunityTarget();
    target.setAddress(GenericAddress.parse(String.format("udp:%s/161", targetIP)));
    target.setCommunity(new OctetString(comunity));
    target.setRetries(2);
    target.setTimeout(1500);
    target.setVersion(SnmpConstants.version1);
    Map<String, String> result = doWalk(oidWalk, target);

    for (String dev : result.keySet()) {
      SNMPDevices device = getMACSignal(targetHOST, targetIP, result.get(dev), comunity);
      if (device != null) {
        snmpDevicesArrayList.add(device);
      }
    }



    return snmpDevicesArrayList;
  }

  private String getDecOfMac(String mac) {
    String[] macSplited = mac.split(":");
    String decMac = "";
    for (String macDec : macSplited) {
      decMac = decMac + "." + Integer.parseInt(macDec, 16);
    }
    return decMac;

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
        setError(true);
        setErrorMSG("Ne mogu da procitam tablu");
        return result;
      }

      for (TreeEvent event : events) {
        if (event == null) {
          continue;
        }

        if (event.isError()) {
          LOGGER.error(event.getErrorMessage());
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
          result.put(variableBinding.getOid().toString(), variableBinding.getVariable().toString());
        }
      }
      snmp.close();
    } catch (IOException e) {
      e.printStackTrace();
    }

    return result;

  }


  public SNMPDevices getMACSignal(String targetHOST, String targetIP, String mac, String comunity) {
    SNMPDevices dev = null;
    String oidWalk = ".1.3.6.1.4.1.41112.1.4.7.1.3.1" + getDecOfMac(mac);
    CommunityTarget target = new CommunityTarget();
    target.setAddress(GenericAddress.parse(String.format("udp:%s/161", targetIP)));
    target.setCommunity(new OctetString(comunity));
    target.setRetries(2);
    target.setTimeout(1500);
    target.setVersion(SnmpConstants.version1);
    Map<String, String> result = doGet(oidWalk, target);

    if (result != null) {
      for (String signal : result.keySet()) {
        System.out.println(String
            .format("MAC: %s, decMAC: %s, SIGNAL: %s", mac, getDecOfMac(mac), result.get(signal)));
        dev = new SNMPDevices();
        dev.setMacDEC(getDecOfMac(mac));
        dev.setMac(mac);
        dev.setHostName(targetHOST);
        dev.setHostAP(targetIP);
        dev.setDateUpdated(LocalDateTime.now().toString());
        dev.setSignal(result.get(signal));
      }
    }

    return dev;

  }

  private Map<String, String> doGet(String oidWalk, CommunityTarget target) {
    Map<String, String> response = null;
    try {
      TransportMapping transportMapping = new DefaultUdpTransportMapping();
      transportMapping.listen();

      CommunityTarget communityTarget = new CommunityTarget();
      communityTarget.setCommunity(target.getCommunity());
      communityTarget.setVersion(target.getVersion());
      communityTarget.setAddress(target.getAddress());
      communityTarget.setRetries(2);
      communityTarget.setTimeout(2000);

      PDU pdu = new PDU();
      pdu.add(new VariableBinding(new OID(oidWalk)));
      pdu.setType(PDU.GET);
      pdu.setRequestID(new Integer32());

      Snmp snmp = new Snmp(transportMapping);
      System.out.println("REQUESTING");
      ResponseEvent responseEvent = snmp.get(pdu, communityTarget);
      if (responseEvent != null) {
        System.out.println("RESPONSE FROM TARGET");
        PDU responsePDU = responseEvent.getResponse();
        if (responsePDU != null) {
          int errorStatus = responsePDU.getErrorStatus();
          int errorIndex = responsePDU.getErrorIndex();
          String errorStratusText = responsePDU.getErrorStatusText();
          if (errorStatus == PDU.noError) {
            System.out.println(responsePDU.getVariableBindings());
            response = new TreeMap<>();
            for (VariableBinding binding : responsePDU.getVariableBindings()) {
              response.put(binding.getOid().toString(), binding.getVariable().toString());
            }
          } else {
            LOGGER.error(String
                .format("ERROR Status: %d, Index: %d, Error, %s ", errorStatus, errorIndex,
                    errorStratusText));

          }
        } else {
          LOGGER.error("ERROR response is NULL");
        }
      } else {
        LOGGER.error("ERROR TIMEOUT TO:" + target.getAddress());
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    return response;

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
