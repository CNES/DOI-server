import { Injectable } from '@angular/core';
import ConfigJson from '../../assets/config.json';

@Injectable({
  providedIn: 'root'
})
export class ConfigService {

  private appConfig: any;

  constructor() { }

  /**
   * Retrieve parameters from config.json configuration file
   */
  async loadAppConfig() {
    this.appConfig = ConfigJson;
  }

  /**
   * Get the URL for call APIs
   */
  get apiBaseUrl() {

    if (!this.appConfig) {
      console.log('Config file not loaded!');
    }

    if (this.appConfig.API_PROTOCOLE != "" && this.appConfig.API_IP != "" && this.appConfig.API_PORT != "") {
      return this.appConfig.API_PROTOCOLE + "://" + this.appConfig.API_IP + ":" + this.appConfig.API_PORT;
    } else {
      // If no data stored, the path is relative on the same machine (client machine = api machine)
      return ""
    }
  }
}