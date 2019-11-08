import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { ApiService } from '../../services/api.service';
import { OrganizationListItem } from '../../apina';
import { LanguageService } from '../../services/language.service';
import { LocationService } from '../../services/location.service';
import { Role, UserService } from 'yti-common-ui/services/user.service';
import { index } from 'yti-common-ui/utils/array';
import { comparingLocalizable } from 'yti-common-ui/utils/comparator';

interface UserOrganizationRoles {
  organization?: OrganizationListItem;
  roles: Role[];
}

@Component({
  selector: 'app-user-details',
  template: `
    <div class="content-box" *ngIf="!loading">

      <app-back-button id="back_button" (back)="back()"></app-back-button>

      <div class="page-header">
        <h1 translate>User details</h1>
      </div>

      <div class="form-group">
        <label translate>Name</label>
        <p class="form-control-static">{{user.name}}</p>
      </div>

      <div class="form-group">
        <label translate>Email</label>
        <p class="form-control-static">{{user.email}}</p>
      </div>

      <div class="form-group">
        <label translate>API token</label>
        <app-information-symbol [infoText]="'INFO_TEXT_TOKEN'"></app-information-symbol>
        <app-inline-clipboard *ngIf="token"
                              [showAsLink]="false"
                              [value]="token"></app-inline-clipboard>
        <div *ngIf="hasExistingToken" class="mb-2">
          <span translate>This user has a token already.</span>
        </div>
        <div>
          <button *ngIf="!hasToken"
                  type="button"
                  id="create_token_button"
                  class="btn btn-action"
                  (click)="createToken()" translate>Create API token</button>
          <button *ngIf="hasToken"
                  type="button"
                  id="delete_token_button"
                  class="btn btn-action"
                  (click)="deleteToken()" translate>Delete API token</button>
        </div>
      </div>

      <div class="form-group">
        <label translate>Organizations and roles</label>
        <div class="form-control-static">
          <div *ngFor="let userOrganization of userOrganizations">
            <div *ngIf="userOrganization.organization">{{userOrganization.organization.name | translateValue}}</div>
            <div *ngIf="!userOrganization.organization" translate>Unknown organization</div>
            <ul>
              <li *ngFor="let role of userOrganization.roles">{{role | translate}}</li>
            </ul>
          </div>
        </div>
      </div>
    </div>
  `
})
export class UserDetailsComponent {

  allOrganizationsById: Map<string, OrganizationListItem>;
  token: string | undefined = undefined;
  tokenDeleted = false;

  constructor(private router: Router,
              private userService: UserService,
              private apiService: ApiService,
              private languageService: LanguageService,
              locationService: LocationService) {

    locationService.atUserDetails();

    apiService.getOrganizationList().subscribe(organizations => {
      this.allOrganizationsById = index(organizations, org => org.id as string);
    });
  }

  get user() {
    return this.userService.user;
  }

  get hasExistingToken(): boolean {
    return this.userService.user.hasToken && this.token === undefined && !this.tokenDeleted;
  }

  get hasToken(): boolean {
    return this.token !== undefined || this.hasExistingToken;
  }

  get loading() {
    return !this.allOrganizationsById;
  }

  get userOrganizations(): UserOrganizationRoles[] {

    const result = Array.from(this.user.rolesInOrganizations.entries()).map(([organizationId, roles]) => {
      return {
        organization: this.allOrganizationsById.get(organizationId),
        roles: Array.from(roles)
      };
    });

    result.sort(comparingLocalizable<UserOrganizationRoles>(this.languageService, org => org.organization ? org.organization.name : {}));

    return result;
  }

  createToken() {

    this.apiService.createToken().subscribe(token => {
      if (token) {
        this.token = token.token;
      } else {
        this.token = undefined;
      }
    })
  }

  deleteToken() {

    this.apiService.deleteToken().subscribe(boolean => {
      if (boolean) {
        this.token = undefined;
        this.tokenDeleted = true;
      }
    })
  }

  back() {
    this.router.navigate(['/']);
  }
}
