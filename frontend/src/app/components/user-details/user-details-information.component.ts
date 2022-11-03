import { Component, OnDestroy } from '@angular/core';
import { Role, UserService, index, comparingLocalizable } from '@vrk-yti/yti-common-ui';
import { Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { LanguageService } from '../../services/language.service';
import { LocationService } from '../../services/location.service';
import { OrganizationListItem } from '../../apina';
import { ApiService } from '../../services/api.service';

interface UserOrganizationRoles {
  organization?: OrganizationListItem;
  roles: Role[];
}

@Component({
  selector: 'app-user-details-information',
  templateUrl: './user-details-information.component.html',
})
export class UserDetailsInformationComponent implements OnDestroy {

  private subscriptionToClean: Subscription[] = [];

  allOrganizationsById: Map<string, OrganizationListItem>;

  token: string | undefined = undefined;
  tokenDeleted = false;

  constructor(private router: Router,
              private userService: UserService,
              private locationService: LocationService,
              private languageService: LanguageService,
              private apiService: ApiService) {

    apiService.getOrganizationList().subscribe(organizations => {
      this.allOrganizationsById = index(organizations, org => org.id as string);
    });

    this.subscriptionToClean.push(this.userService.loggedIn$.subscribe((loggedIn: boolean) => {
      if (!loggedIn) {
        router.navigate(['/']);
      }
    }));

    userService.updateLoggedInUser();

    locationService.atUserDetails();
  }

  ngOnDestroy() {

    this.subscriptionToClean.forEach(s => s.unsubscribe());
  }

  get user() {

    return this.userService.user;
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

  get hasExistingToken(): boolean {
    return this.userService.user.hasToken && this.token === undefined && !this.tokenDeleted;
  }

  get hasToken(): boolean {
    return this.token !== undefined || this.hasExistingToken;
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
}
