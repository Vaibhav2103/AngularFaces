/**
 *  (C) Stephan Rauh http://www.beyondjava.net
 */
package de.beyondjava.jsf.ajax.differentialContextWriter.differenceEngine;

import static de.beyondjava.jsf.ajax.differentialContextWriter.differenceEngine.DOMUtils.stringToDOM;

import java.io.*;
import java.util.*;
import java.util.logging.*;

import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.FileUtils;
import org.w3c.dom.*;

/**
 * @author Stephan Rauh http://www.beyondjava.net
 * 
 */
public class DiffenceEngine {
   private static final Logger LOGGER = Logger
         .getLogger("de.beyondjava.jsf.ajax.differentialContextWriter.differenceEngine.DiffenceEngine");

   final String LAST_KNOWN_HTML_KEY = "com.beyondEE.faces.diff.lastKnownHTML";

   /**
    * 
    */
   public DiffenceEngine() {
   }

   /**
    * @param currentResponse
    * @return
    */
   private String DEBUGdeleteCityNode(String currentResponse) {
      int pos = currentResponse.indexOf("</changes></partial-response>");
      String test = "<delete id=\"j_idt3:zipcode\"></delete>";
      currentResponse = currentResponse.substring(0, pos) + test + currentResponse.substring(pos);
      return currentResponse;
   }

   /**
    * @param change
    * @param lastKnowDOMTree
    */
   private ArrayList<Node> determineNecessaryChangeFromResponse(Node change, Document lastKnowDOMTree,
         ArrayList<String> deletions, ArrayList<String> changes) {
      if (change.getNodeName().equals("update")) {
         // DOMUtils.domToString(partialChangeNode);
         String id = change.getAttributes().getNamedItem("id").getNodeValue();
         String nodeValue = change.getFirstChild().getNodeValue();
         String changingHTML = nodeValue.toString().trim();
         Node lastKnownCorrespondingNode = findNodeWithID(id, lastKnowDOMTree);
         ArrayList<Node> necessaryChanges = determineNecessaryChanges(changingHTML, lastKnownCorrespondingNode,
               deletions, changes);
         ArrayList<Node> partialChanges = new ArrayList<Node>();
         if (null != necessaryChanges) {
            // Todo create an array of partial changes
            for (Node n : necessaryChanges) {
               String partialUpdate = DOMUtils.domToString(n);
               String partialID = ((Element) n).getAttribute("id");
               String partialChange = "<update id=\"" + partialID + "\"><![CDATA[" + partialUpdate + "]]></update>";

               Node partialChangeNode = DOMUtils.stringToDOM(partialChange);
               partialChanges.add(partialChangeNode);
            }
         }
         if (null != deletions) {
            // Todo create an array of partial changes
            for (String partialID : deletions) {
               String partialChange = "<delete id=\"" + partialID + "\"/>";
               Node partialChangeNode = DOMUtils.stringToDOM(partialChange);
               partialChanges.add(partialChangeNode);
            }
         }
         if (null != changes) {
            // Todo create an array of partial changes
            for (String partialChange : changes) {
               Node partialChangeNode = DOMUtils.stringToDOM(partialChange);
               partialChanges.add(partialChangeNode);
            }
         }

         if (partialChanges.size() > 0) {
            return partialChanges;
         }
         else {
            return null;
         }
      }
      else {
         LOGGER.info(change.getNodeName() + " - remains unchanged");
      }
      change.getFirstChild().getNodeName();
      return null;

   }

   /**
    * @param changingHTML
    * @param lastKnownCorrespondingNode
    * @param deletions2
    * @param changes2
    */
   protected ArrayList<Node> determineNecessaryChanges(String newHTML, Node lastKnownCorrespondingNode,
         ArrayList<String> deletions, ArrayList<String> changes) {
      if (newHTML.startsWith("<")) {
         Node newDOM = stringToDOM(newHTML).getFirstChild();
         ArrayList<Node> differences = XmlDiff.getDifferenceOfNodes(lastKnownCorrespondingNode, newDOM, deletions,
               changes);
         for (Node d : differences) {
            LOGGER.info("Difference: " + d);
         }
         for (String d : deletions) {
            LOGGER.info("Deletion: " + d);
         }
         for (String d : changes) {
            LOGGER.info("Change: " + d);
         }
         // generateJUnitTest(newHTML, lastKnownCorrespondingNode, differences);
         return differences;

      }
      else {
         return null;
      }

   }

   String domToString(Node node) {
      try {
         TransformerFactory tf = TransformerFactory.newInstance();
         Transformer transformer = tf.newTransformer();
         transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
         StringWriter writer = new StringWriter();
         transformer.transform(new DOMSource(node), new StreamResult(writer));
         return writer.toString();
      }
      catch (TransformerException te) {
         return "(TransformerException)";
      }
   }

   /**
    * @param partialResponseAsDOMTree
    * @return
    */
   private List<Node> extractChangesFromPartialResponse(Document partialResponseAsDOMTree) {
      // NodeList partialResponses =
      // partialResponseAsDOMTree.getElementsByTagName("partial-response");
      List<Node> partialResponses = new ArrayList<Node>();
      Node partialNode = partialResponseAsDOMTree.getFirstChild();
      Node changes = partialNode.getFirstChild();
      for (int i = 0; i < changes.getChildNodes().getLength(); i++) {
         Node n = changes.getChildNodes().item(i);
         partialResponses.add(n);
      }
      return partialResponses;
   }

   /**
    * @param id
    * @param lastKnowDOMTree
    * @return
    */
   private Node findNodeWithID(String id, Node tree) {
      if (null != tree.getAttributes()) {
         final Node idNode = tree.getAttributes().getNamedItem("id");
         if (null != idNode) {
            if (id.equals(idNode.getNodeValue())) {
               return tree;
            }
         }
      }
      int length = tree.getChildNodes().getLength();
      for (int i = 0; i < length; i++) {
         Node result = findNodeWithID(id, tree.getChildNodes().item(i));
         if (null != result) {
            return result;
         }
      }
      return null;
   }

   /**
    * @param newHTML
    * @param lastKnownCorrespondingNode
    * @param differences
    */
   private void generateJUnitTest(String newHTML, Node lastKnownCorrespondingNode, ArrayList<Node> differences) {
      File dir = new File("E:/this/AngularFaces/src/test/resources/DifferenceEngine");
      if (dir.exists()) {
         int freeNumber = 0;
         File testcase;
         File partialChange;
         do {
            freeNumber++;
            testcase = new File(dir, "html" + freeNumber + ".xml");
            partialChange = new File(dir, "partialChange" + freeNumber + ".xml");

         } while (testcase.exists());
         try {
            FileUtils.write(testcase, domToString(lastKnownCorrespondingNode));
            FileUtils.write(partialChange, newHTML);
            int index = 1;
            for (Node d : differences) {
               File differenceFile = new File(dir, "difference" + freeNumber + "_" + index + ".xml");
               FileUtils.write(differenceFile, domToString(d));
            }
         }
         catch (IOException e) {
            LOGGER.severe("Couldn't create JUnit test case");
         }

      }

   }

   /**
    * @param sessionMap
    * @return
    */
   private String retrieveLastKnownHTMLFromSession(Map<String, Object> sessionMap) {
      String html = (String) sessionMap.get(LAST_KNOWN_HTML_KEY);
      if (html == null) {
         throw new RuntimeException("HTML code is missing in the session");
      }
      return html;
   }

   /**
    * Compares the current HTML response with the last known HTML code. If it's
    * a regular HTML response, the HTML code is simply stored in the session. If
    * it's an JSF AJAX response, the method looks at the differences and tries
    * to remove unchanged HTML from the response.
    * 
    * @param rawBuffer
    * @param sessionMap
    * @param isAJAX
    * @return
    */
   public String yieldDifferences(String currentResponse, Map<String, Object> sessionMap, boolean isAJAX) {
      if (isAJAX) {
         String html = retrieveLastKnownHTMLFromSession(sessionMap);
         Document lastKnowDOMTree = stringToDOM("<original>" + html + "</original>");
         Document partialResponseAsDOMTree = stringToDOM(currentResponse);
         List<Node> listOfChanges = extractChangesFromPartialResponse(partialResponseAsDOMTree);
         for (Node change : listOfChanges) {
            ArrayList<String> deletions = new ArrayList<String>();
            ArrayList<String> changes = new ArrayList<String>();
            ArrayList<Node> newPartialChanges = determineNecessaryChangeFromResponse(change, lastKnowDOMTree,
                  deletions, changes);
            if ((null != newPartialChanges) && (newPartialChanges.size() > 0)) {
               String id = ((Element) change).getAttribute("id");
               int start = currentResponse.indexOf("<update id=\"" + id + "\">");
               int end = currentResponse.indexOf("</update>", start);
               String currentResponseEnd = currentResponse.substring(end + "</update>".length());
               try {
                  String tmpCurrentResponse = currentResponse.substring(0, start);
                  for (Node n : newPartialChanges) {
                     String c = domToString(n);
                     tmpCurrentResponse += c;
                     String nodeid = ((Element) n.getFirstChild()).getAttribute("id");
                     String newHTML = n.getFirstChild().getFirstChild().getNodeValue();
                     Node nodeToReplace = DOMUtils.stringToDOM(newHTML).getFirstChild();
                     nodeToReplace = lastKnowDOMTree.importNode(nodeToReplace, true);
                     Node nodeToBeReplaced = findNodeWithID(nodeid, lastKnowDOMTree);
                     Node parentNode = nodeToBeReplaced.getParentNode();
                     parentNode.replaceChild(nodeToReplace, nodeToBeReplaced);
                  }
                  currentResponse = tmpCurrentResponse + currentResponseEnd;
               }
               catch (Exception e) {
                  LOGGER.log(Level.SEVERE, "An exception occured while trying to replace the response", e);
               }
            }
            String newHTML = DOMUtils.domToString(lastKnowDOMTree.getFirstChild());
            if (newHTML.startsWith("<original>")) {
               newHTML = newHTML.substring("<original>".length());
               newHTML = newHTML.substring(0, newHTML.length() - "</original>".length());
            }
            sessionMap.remove(LAST_KNOWN_HTML_KEY);
            sessionMap.put(LAST_KNOWN_HTML_KEY, newHTML);
         }
         // currentResponse = DEBUGdeleteCityNode(currentResponse);
      }
      else {
         sessionMap.remove(LAST_KNOWN_HTML_KEY);
         sessionMap.put(LAST_KNOWN_HTML_KEY, currentResponse);
      }
      return currentResponse;
   }

}
