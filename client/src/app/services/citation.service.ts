import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { ConfigService } from './config.service';
import { Observable, Subject } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class CitationService {

    // Subject for retrieve styles
    private subjectCitationStyles = new Subject<any[]>();

    // Subject for retrieve languages
    private subjectCitationLanguages = new Subject<any[]>();

    constructor(private http: HttpClient,
                private configService: ConfigService) {}

     /**
     * Get all citation styles from server
     */
    public getStylesFromServer() {
        this.http.get<string[]>(this.configService.apiBaseUrl + '/citation/style').forEach(elements => {
            this.subjectCitationStyles.next(elements);
        })
    }

    /**
     * Get all citation styles (observable / subscription)
     */
    public getStyles(): Observable<string[]> {
        return this.subjectCitationStyles.asObservable();
    }


     /**
     * Get all citation languages from server
     */
      public getLanguagesFromServer() {
        this.http.get<string[]>(this.configService.apiBaseUrl + '/citation/language').forEach(elements => {
            this.subjectCitationLanguages.next(elements);
        })
    }

    /**
     * Get all citation languages (observable / subscription)
     */
    public getLanguages(): Observable<string[]> {
        return this.subjectCitationLanguages.asObservable();
    }

     /**
     * Get citation from server
     */
      public getCitationFromServer(doi: string, lang: string, style: string) {
        return this.http.get<any>(this.configService.apiBaseUrl + '/citation/format?doi=' + doi + "&lang=" + lang + '&style=' + style);
    }
}