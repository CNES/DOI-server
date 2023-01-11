import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class XmlService {

  constructor() { }

  /**
   * Initialize XML object
   * @param rootElem 
   * @returns 
   */
  public initXml(rootElem: string) {
    return document.implementation.createDocument(null, rootElem);
  }

  /**
   * Set child-tag 'tag' in the document 'xmlDoc', of the tag 'rootElem', with in option the text 'value'
   * MORE : https://www.w3schools.com/xml/dom_nodes_create.asp
   * @param xmlDoc 
   * @param rootElem 
   * @param tag 
   * @param value 
   */
  public setElement(xmlDoc: XMLDocument, rootElem: string, tag: string, value?: string) {
    let element = xmlDoc.createElement(tag);
    if(value!=undefined){
      let newText = xmlDoc.createTextNode(value);
      element.appendChild(newText);
    }
    xmlDoc.getElementsByTagName(rootElem)[0].appendChild(element);
  }

  /**
   * Set an attribute (key=attribute and value=text) on the element, in the document 'xmlDoc'
   * @param xmlDoc 
   * @param element 
   * @param attribute 
   * @param text 
   */
  public setAttribute(xmlDoc: XMLDocument, element: string, attribute: string, text: string) {
    let newAtt = xmlDoc.createAttribute(attribute);
    newAtt.nodeValue = text;
    xmlDoc.getElementsByTagName(element)[0].setAttributeNode(newAtt);
  }

  /**
   * Get the key with the value of an anum
   * @param myEnum The enum
   * @param enumValue The value
   * @returns The key
   */
  public getEnumKeyByValue(myEnum: any, enumValue: any): string {
    let keys = Object.keys(myEnum).filter(x => myEnum[x] == enumValue);
    return keys.length > 0 ? keys[0] : '';
  }
  
  /**
   * Get the value with the key of an anum
   * @param myEnum The enum
   * @param enumValue The value
   * @returns The key
   */
   public getEnumValueByKey(myEnum: any, enumKey: any): string {
     return myEnum[enumKey];
    // let values: string[] = Object.values(myEnum).filter(myEnum[enumKey]);
    // return values.length > 0 ? values[0] : '';
  }
}
