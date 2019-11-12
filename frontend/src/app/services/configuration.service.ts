import { Injectable } from '@angular/core';
import { ApiService } from './api.service';
import { ConfigurationModel } from "../apina";
import { Config } from "../entities/config";

@Injectable({
  providedIn: 'root',
})
export class ConfigurationService {

  private configuration: Config;

  constructor(private apiService: ApiService) {
  }

  fetchConfiguration(): Promise<ConfigurationModel> {
    return this.apiService.getConfiguration().toPromise().then(configuration => {
      this.configuration = configuration;
      return configuration;
    });
  }

  get loading(): boolean {
    return this.configuration == null;
  }

  get env(): string {
    return this.configuration.env;
  }

  get isMessagingEnabled(): boolean {
    return this.configuration.messagingEnabled;
  }

  get terminologyUrl(): string {
    return this.configuration.terminologyUrl;
  }

  get dataModelUrl(): string {
    return this.configuration.dataModelUrl;
  }

  get codeListUrl(): string {
    return this.configuration.codeListUrl;
  }

  get commentsUrl(): string {
    return this.configuration.commentsUrl;
  }

  get fakeLoginAllowed(): boolean {
    return this.configuration.fakeLoginAllowed;
  }

  getUriWithEnv(uri: string): string | null {

    if (uri && this.env !== 'prod') {
      return uri + '?env=' + this.env;
    }
    return uri ? uri : null;
  }

  getEnvironmentIdentifier(style?: 'prefix' | 'postfix'): string {
    if (this.env !== 'prod') {
      const identifier = this.env.toUpperCase();
      if (!style) {
        return identifier;
      } else if (style === 'prefix') {
        return identifier + ' - ';
      } else if (style === 'postfix') {
        return ' - ' + identifier;
      }
    }
    return '';
  }
}
