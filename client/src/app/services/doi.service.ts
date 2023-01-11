import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { ConfigService } from './config.service';

@Injectable({
  providedIn: 'root'
})
export class DoiService {

  constructor(
    private http: HttpClient,
    private configService: ConfigService) { }

  /**
   * Get the INIST code form server
   */
  public getInistCode() {
    return this.http.get<string>(this.configService.apiBaseUrl + '/mds/inist');
  }

  /**
   * Get DOI metadata
   * @param project 
   * @param doi
   * @returns the metadatas or error
   */
  public getMetadataFromDoi(project: string, doi: string) {
    // Build header
    let option = { headers: new HttpHeaders().set('selectedRole', project).set('Content-Type', 'text/xml') };

    return this.http.get<any>(this.configService.apiBaseUrl + '/mds/metadata/' + doi, option);
  }

  /**
   * Get DOI URL
   * @param project 
   * @param doi
   * @returns the metadatas or error
   */
   public getUrlFromDoi(project: string, doi: string) {
    // Build header
    let option = { headers: new HttpHeaders().set('selectedRole', project).set('Content-Type', 'text/xml') };

    return this.http.get<any>(this.configService.apiBaseUrl + '/mds/dois/' + doi, option);
  }

  /**
   * Deactivate a DOI
  * @param project the current project
  * @param doi the DOI to deactivate
  * @returns error
   */
  public deactivateDoi(project: string, doi: string) {
    // Build header
    let option = { headers: new HttpHeaders().set('selectedRole', project) };

    return this.http.delete<any>(this.configService.apiBaseUrl + '/mds/metadata/' + doi, option);
  }

  /**
   * Create a DOI
   */
  public createOrModifyDOI(projectId: string, sXml: string) {
       // Build header
       let option = { headers: new HttpHeaders().set('Content-Type', 'application/xml; charset=UTF-8')
                                                .set('selectedRole', projectId)};

       // Launch the request
       return this.http.post<any>(this.configService.apiBaseUrl + '/mds/metadata', sXml, option);
  }

  /**
   * Add URL on DOI
   */
   public createOrModifyURL(projectId: string, doiName: string, url: string) {
    // Build body
    let body = new HttpParams().set('doi', doiName).set('url', url);

    // Build header
    let option = { headers: new HttpHeaders().set('selectedRole', projectId)};

    // Launch the request
    return this.http.post<any>(this.configService.apiBaseUrl + '/mds/dois', body, option);
   }

   /**
    * Validate the XML with the XSD on server
    * @param sXml the xml to validate
    * @returns data  = true if it's valid, error otherwise
    */
   public validateXmlWithXsd(sXml: string) {
       // Build header
       let option = { headers: new HttpHeaders().set('Content-Type', 'application/xml; charset=UTF-8') };

       // Launch the request
       return this.http.post<any>(this.configService.apiBaseUrl + '/mds/metadata/xsd', sXml, option);
   }
}
