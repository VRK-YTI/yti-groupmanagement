import { Injectable } from '@angular/core';
import { UUID } from '../apina';
import { UserService, User, contains } from '@vrk-yti/yti-common-ui';

@Injectable()
export class AuthorizationManager {

  constructor(private userService: UserService) {
  }

  get user(): User {
    return this.userService.user;
  }

  canCreateOrganization(): boolean {
    return this.user ? this.user.superuser : false;
  }

  canBrowseUsers(): boolean {
    return this.user.superuser || this.isAdminInAnyOrganization();
  }

  canViewOrganization(organizationId: UUID) {
    return this.user.superuser || this.user.isInRole('ADMIN', organizationId as string);
  }

  canEditOrganization(organizationId: UUID) {
    return this.user.superuser || this.user.isInRole('ADMIN', organizationId as string);
  }

  private isAdminInAnyOrganization() {
    return contains(Array.from(this.user.organizationsInRole.keys()), 'ADMIN');
  }

  canRemoveOrganization(): boolean {
    return this.user.superuser;
  }

  canRemoveUser(): boolean {
    return this.user.superuser;
  }

}
