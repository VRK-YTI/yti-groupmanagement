import { Component } from '@angular/core';
import { OrganizationListItem, UUID } from '../apina';
import { LocationService } from '../services/location.service';
import { ApiService } from '../services/api.service';
import { Observable, BehaviorSubject, Subject, combineLatest } from 'rxjs';
import { Localizable } from 'yti-common-ui/types/localization';
import { requireDefined } from 'yti-common-ui/utils/object';
import { index } from 'yti-common-ui/utils/array';
import { FilterOptions } from 'yti-common-ui/components/filter-dropdown.component';
import { LanguageService } from '../services/language.service';
import { TranslateService } from '@ngx-translate/core';
import { User } from '../entities/user';
import { UserService } from 'yti-common-ui/services/user.service';
import { AuthorizationManager } from '../services/authorization-manager.service';
import { ignoreModalClose } from 'yti-common-ui/utils/modal';
import { DeleteConfirmationModalService } from './delete-confirmation-modal.component';
import { map } from 'rxjs/operators';

@Component({
  selector: 'app-users',
  template: `
    <app-ajax-loading-indicator *ngIf="loading"></app-ajax-loading-indicator>

    <h1 translate>Users</h1>

    <div class="top-actions">

      <div class="input-group input-group-search float-left">
        <input #searchInput
               id="search_user_input"
               type="text"
               class="form-control"
               placeholder="{{'Search user' | translate}}"
               [(ngModel)]="search" />
      </div>

      <app-filter-dropdown [options]="organizationOptions"
                           id="organizations_dropdown"
                           [filterSubject]="organization$"
                           class="float-left ml-2"></app-filter-dropdown>

      <app-filter-dropdown [options]="roleOptions"
                           id="roles_dropdown"
                           [filterSubject]="role$"
                           class="float-left ml-2"></app-filter-dropdown>
    </div>

    <div class="results">
      <div class="result" *ngFor="let user of users$ | async">
        <h4>{{user.displayName}} <span class="email">({{user.email}})</span>
          <button class="btn btn-link btn-sm"
                  id="{{user.email + '_remove_user_button'}}"
                  (click)="removeUser(user)"
                  *ngIf="canRemoveUser()">
            <span class="fa fa-trash"></span>
            <span translate>Remove</span>
          </button>
          <div id="time">{{user.creationDateTime | dateTime }}</div>
          <div *ngIf="user.superuser" id="superuser"><br translate>SuperUser</div>
        </h4>

        <ul>
          <li *ngFor="let organization of user.organizations">
            <a [routerLink]="['/organization', organization.id]" id="{{'organization_listitem_' + organization.id}}">
              {{organization.name | translateValue}}
            </a>:
            <span *ngFor="let role of organization.roles; let last = last">
                <span class="role" id="{{'role_item_' + role}}">{{role | translate}}</span><span [hidden]="last">,</span>
              </span>
          </li>
        </ul>
        <br>
      </div>
    </div>
  `,
  styleUrls: ['./users.component.scss']
})

export class UsersComponent {

  roleOptions: FilterOptions<string>;
  organizationOptions: FilterOptions<OrganizationListItem>;

  search$ = new BehaviorSubject('');
  role$ = new BehaviorSubject<string|null>(null);
  organization$ = new BehaviorSubject<OrganizationListItem|null>(null);
  usersForOwnOrganizations = new Subject<User[]>();

  users$: Observable<UserViewModel[]>;

  constructor(private apiService: ApiService,
              private locationService: LocationService,
              languageService: LanguageService,
              translateService: TranslateService,
              userService: UserService,
              private authorizationManager: AuthorizationManager,
              private deleteUserModal: DeleteConfirmationModalService) {

    this.refreshUsers();

    this.apiService.getAllRoles().subscribe(roles => {
      this.roleOptions = [null, ...roles].map(role => ({
        value: role,
        name: () => translateService.instant(role ? role : 'All roles')
      }));
    });

    const organizations$ = this.apiService.getOrganizationListWithChildren();

    organizations$.subscribe(organizations => {

      const ownOrganizations = organizations.filter(org => {

        const user = userService.user;
        return user.superuser || user.isInRole('ADMIN', org.id as string);
      });

      this.organizationOptions = [null, ...ownOrganizations].map(org => ({
        value: org,
        name: () => org ? languageService.translate(org.name) : translateService.instant('All organizations')
      }));
    });

    this.users$ = combineLatest(organizations$, this.usersForOwnOrganizations, this.search$, this.role$, this.organization$)
      .pipe(map(([organizations, users, search, role, organization]) => {

        const roleMatches = (user: UserViewModel) =>
          !role || user.organizations.find(org => org.roles.indexOf(role) !== -1);

        const organizationMatches = (user: UserViewModel) =>
          !organization || user.organizations.find(org => org.id === organization.id) != null;

        const searchMatchesName = (user: UserViewModel) =>
          !search || user.displayName.toLowerCase().indexOf(search.toLowerCase()) !== -1;

        const searchMatchesEmail = (user: UserViewModel) =>
          !search || user.email.toLowerCase().indexOf(search.toLowerCase()) !== -1;

        const searchMatches = (user: UserViewModel) =>
          searchMatchesName(user) || searchMatchesEmail(user);

        return users.map(user => new UserViewModel(user, index(organizations, org => org.id)))
          .filter(user => roleMatches(user) && organizationMatches(user) && searchMatches(user));
      }));
  }

  refreshUsers() {
    this.apiService.getUsersForOwnOrganizations()
      .subscribe(users => this.usersForOwnOrganizations.next(users));
  }

  get loading() {
    return this.roleOptions == null || this.organizationOptions == null;
  }

  get organization(): OrganizationListItem|null {
    return this.organization$.getValue();
  }

  set organization(value: OrganizationListItem|null) {
    this.organization$.next(value);
  }

  get search() {
    return this.search$.getValue();
  }

  set search(value: string) {
    this.search$.next(value);
  }

  canRemoveUser(): boolean {
    return this.authorizationManager.canRemoveUser();
  }

  removeUser(user: UserViewModel) {
    this.deleteUserModal.open(user.displayName, user.email, 'This user will be removed.')
      .then(() => {
        this.apiService.removeUser(user.email)
          .subscribe(() => this.refreshUsers());
      }, ignoreModalClose);
  }
}

class UserViewModel {

  organizations: { id: UUID, name: Localizable, roles: string[] }[];

  constructor(private user: User, organizations: Map<UUID, OrganizationListItem>) {

    this.organizations = user.organizations.map(org => {

      return {
        id: org.id,
        name: requireDefined(organizations.get(org.id)).name,
        roles: org.roles
      };
    });
  }

  get email() {
    return this.user.email;
  }

  get displayName() {
    return this.user.lastName + ', ' + this.user.firstName;
  }

  get superuser() {
    return this.user.superuser;
  }

  get creationDateTime() {
    return this.user.creationDateTime;
  }
}
