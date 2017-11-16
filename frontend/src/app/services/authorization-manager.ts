import { Injectable } from '@angular/core';
import { AuthenticatedUser } from '../entities/authenticated-user';
import { AuthenticationEndpoint } from '../apina';

@Injectable()
export class AuthorizationManager {

  user?: AuthenticatedUser;

  constructor(private endpoint: AuthenticationEndpoint) {
    this.endpoint.getAuthenticatedUser()
      .map(user => new AuthenticatedUser(user))
      .subscribe(user => this.user = user);
  }

  canCreateOrganization(): boolean {
    return this.user ? this.user.superuser : false;
  }

  canBrowseUsers(): boolean {
    return this.user ? !this.user.superuser || this.user.isAdminInAnyOrganization() : false;
  }
}