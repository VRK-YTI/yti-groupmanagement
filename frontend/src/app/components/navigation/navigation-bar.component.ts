import { Component } from '@angular/core';
import { Language, LanguageService } from '../../services/language.service';
import { UserService } from 'yti-common-ui/services/user.service';
import { LoginModalService } from 'yti-common-ui/components/login-modal.component';
import { ApiService } from '../../services/api.service';
import {ConfigurationService} from "../../services/configuration.service";

@Component({
  selector: 'app-navigation-bar',
  styleUrls: ['./navigation-bar.component.scss'],
  template: `
    <nav class="navbar navbar-expand-md navbar-light">

      <div class="navbar-header">
        <a id="main_page_link" class="navbar-brand" [routerLink]="['/']">
          <!--<app-logo></app-logo>-->
          <span translate>Interoperability platform's user right management</span>
          <span *ngIf="environmentIdentifier">{{environmentIdentifier}}</span>
        </a>
      </div>

      <ul class="navbar-nav ml-auto">

        <li *ngIf="fakeableUsers.length > 0" class="nav-item dropdown" ngbDropdown>
          <a class="nav-link" id="fakeable_user_dropdown" ngbDropdownToggle translate>Impersonate user</a>
          <div ngbDropdownMenu>
            <a class="dropdown-item" 
               *ngFor="let user of fakeableUsers" 
               (click)="fakeUser(user.email)" 
               id="{{user.email + '_fakeable_user_link'}}">
              {{user.firstName}} {{user.lastName}}
            </a>
          </div>
        </li>
        
        <li class="nav-item" *ngIf="!isLoggedIn()">
          <a class="nav-link" id="log_in_link" (click)="logIn()" translate>LOG IN</a>
        </li>

        <li class="nav-item logged-in" *ngIf="isLoggedIn()">
          <span>{{user.name}}</span>
          <a class="nav-link" id="log_out_link" (click)="logOut()" translate>LOG OUT</a>
        </li>

        <li class="nav-item dropdown" placement="bottom-right" ngbDropdown>
          <a class="dropdown-toggle nav-link btn btn-language" id="lang_selection_dropdown" ngbDropdownToggle>{{language.toUpperCase()}}</a>
          <div ngbDropdownMenu>
            <a *ngFor="let availableLanguage of availableLanguages"
               id="{{availableLanguage.code + '_available_language'}}"
               class="dropdown-item"
               [class.active]="availableLanguage.code === language"
               (click)="language = availableLanguage.code">
              <span>{{availableLanguage.name}}</span>
            </a>
          </div>
        </li>

        <li class="nav-item dropdown" placement="bottom-right" ngbDropdown>
          <a class="nav-link btn-menu" id="app_menu_dropdown" ngbDropdownToggle>
            <app-menu></app-menu>
          </a>
          <div ngbDropdownMenu>
            <a class="dropdown-item" id="navigation_log_out_link" *ngIf="isLoggedIn()" (click)="logOut()">
              <i class="fas fa-sign-out-alt"></i>
              <span translate>LOG OUT</span>
            </a>
            <a class="dropdown-item" id="navigation_log_in_link" *ngIf="!isLoggedIn()" (click)="logIn()">
              <i class="fas fa-sign-in-alt"></i>
              <span translate>LOG IN</span>
            </a>
            <div class="dropdown-divider"></div>
            <a class="dropdown-item" id="navigation_link_user_details" [routerLink]="['/userDetails']" translate>User details</a>
            <a class="dropdown-item" id="navigation_interoperability_platform_link" href="https://yhteentoimiva.suomi.fi" target="_blank" translate>yhteentoimiva.suomi.fi</a>
            <a class="dropdown-item" id="navigation_reference_data_link" [href]="configService.codeListUrl" target="_blank" translate>Suomi.fi Reference Data</a>
            <a class="dropdown-item" id="navigation_terminologies_link" [href]="configService.terminologyUrl" target="_blank" translate>Suomi.fi Terminologies</a>
            <a class="dropdown-item" id="navigation_data_vocabularies_link" [href]="configService.dataModelUrl" target="_blank" translate>Suomi.fi Data Vocabularies</a>
          </div>
        </li>
      </ul>
    </nav>
  `
})
export class NavigationBarComponent {

  availableLanguages = [
    { code: 'fi' as Language, name: 'Suomeksi (FI)' },
    // { code: 'sv' as Language, name: 'På svenska (SV)' },
    { code: 'en' as Language, name: 'In English (EN)' }
  ];

  fakeableUsers: { email: string, firstName: string, lastName: string }[] = [];

  codeListUrl: string;
  terminologyUrl: string;
  dataModelUrl: string;
  fakeLoginAllowed: boolean;


  constructor(private languageService: LanguageService,
              private userService: UserService,
              private loginModal: LoginModalService,
              private apiService: ApiService,
              public configService: ConfigurationService) {


    apiService.getUsers().subscribe(users => {
      if (this.configService.fakeLoginAllowed) {
        this.fakeableUsers = users.map(u => ({email: u.email, firstName: u.firstName, lastName: u.lastName}));
      }
    });
  }

  fakeUser(userEmail: string) {
    this.userService.updateLoggedInUser(userEmail);
  }

  set language(language: Language) {
    this.languageService.language = language;
  }

  get language(): Language {
    return this.languageService.language;
  }

  logIn() {
    this.loginModal.open();
  }

  logOut() {
    this.userService.logout();
  }

  get user() {
    return this.userService.user;
  }

  isLoggedIn() {
    return this.userService.isLoggedIn();
  }

  get environmentIdentifier() {
    const env = this.configService.env;
    return env ? env !== 'prod' ? ' - ' + env.toUpperCase() : '' : '';
  }

}
