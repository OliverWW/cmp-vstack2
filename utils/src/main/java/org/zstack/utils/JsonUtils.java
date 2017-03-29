package org.zstack.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Created by xing5 on 2016/5/18.
 */
public class JsonUtils {

	
	
	public static String getInstanceID(String cloudType,String cpuNum,String memory){
	 String filename="../webapps/zstack/WEB-INF/classes/pubCloudInfo.xml";
		List<PubCloud> PubCloud = getPubCloudConf(filename);
		if(PubCloud.size()!=0){
			for(PubCloud tmp : PubCloud){
				if(tmp.getName().equals(cloudType)){
					List<InstanceMode> tmpMD = tmp.getInstanceMD();
					if(tmpMD.size()!=0){
						for(InstanceMode mode : tmpMD){
							if(mode.getCpuNum().equals(cpuNum) && mode.getMemory().equals(memory)){
								return mode.getUuid();
							}
						}
					}
				}
			}
		}
		return null;
	}
	
	
	
	public static PubCloud getPubCloudConfByType(String type){
		String filename="../webapps/zstack/WEB-INF/classes/pubCloudInfo.xml";
		List<PubCloud> PubCloud = getPubCloudConf(filename);
		PubCloud cloud = new PubCloud();
		if(PubCloud.size()!=0){
			for(PubCloud tmp : PubCloud){
				if(tmp.getName().equals(type)){
					cloud = tmp;
					break;
				}
				
			}
		}
		return cloud;
	}
	
	
	
	public static List<PubCloud> getPubCloudConf(String fileName){
		DocumentBuilder builder;
		Document doc = null;
		List<PubCloud>  pubClouds = new ArrayList<PubCloud>();
		
		pubClouds = new ArrayList<PubCloud>();
		try {
			File xmlFile = new File(fileName);
			DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
			builder = builderFactory.newDocumentBuilder();
			doc = builder.parse(xmlFile);
			doc.getDocumentElement().normalize();
		} catch (SAXException | IOException e1) {
			e1.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}

		NodeList nList = doc.getElementsByTagName("pubCloud");
		for (int i = 0; i < nList.getLength(); i++) {
			Node node = nList.item(i);
			Element ele = (Element) node;
			
			PubCloud tmpCloud = new PubCloud();
			tmpCloud.setName(ele.getElementsByTagName("name").item(0).getTextContent());   
			//TODO 判断为不同的云种类后，返回不同的对象模型（）。还是并集？
			
			NodeList invernts = ele.getElementsByTagName("InstanceModel");
			if(invernts.getLength()!=0){
				List<InstanceMode> instanceMDs = new ArrayList<InstanceMode>();
				if(invernts.getLength()!=0){
					for(int j = 0;j<invernts.getLength();j++){
						Node temnode = invernts.item(j);
						Element temele = (Element) temnode;
						InstanceMode temMode = new InstanceMode();
						temMode.setCpuNum(temele.getElementsByTagName("cpuNum").item(0).getTextContent());
						temMode.setUuid(temele.getElementsByTagName("uuid").item(0).getTextContent());
						temMode.setMemory(temele.getElementsByTagName("memory").item(0).getTextContent());
						instanceMDs.add(temMode);
					}
					tmpCloud.setInstanceMD(instanceMDs);
				}
			}
			
			
			
			NodeList images = ele.getElementsByTagName("image");
			if(images.getLength()!=0){
				List<String> imas = new ArrayList<String>();
				if(images.getLength()!=0){
					for(int j = 0;j<images.getLength();j++){
						imas.add( images.item(j).getTextContent());
					}
					tmpCloud.setImages(imas);
				}

			}
			
			NodeList accountInfos = ele.getElementsByTagName("accountInfo");
			if(accountInfos.getLength()!=0){
				List<String> infos = new ArrayList<String>();
				if(accountInfos.getLength()!=0){
					for(int j = 0;j<accountInfos.getLength();j++){
						infos.add( accountInfos.item(j).getTextContent());
					}
					tmpCloud.setAccountInfo(infos);
				}
			}
			
			pubClouds.add(tmpCloud);	 
		}
		return pubClouds;
		
	}
}
