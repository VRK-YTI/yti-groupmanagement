import { Component, ViewChild } from '@angular/core';
import { LocationService } from '../services/location.service';
import { SearchUserModalService } from './search-user-modal.component';
import { ignoreModalClose } from 'yti-common-ui/utils/modal';
import { User } from '../entities/user';
import { OrganizationDetails } from '../entities/organization-details';
import { Router } from '@angular/router';
import { ApiService } from '../services/api.service';
import { NotificationDirective } from 'yti-common-ui/components/notification.component';
import { TranslateService } from '@ngx-translate/core';
import { OrganizationDetailsComponent } from './organization-details.component';

@Component({
  selector: 'app-new-organization',
  template: `
    <div class="content-box">

      <app-back-button (back)="back()"></app-back-button>

      <div class="clearfix">
        <h1 class="float-left" translate>New organization</h1>        
        <button type="button"
                id="save_organization_button"
                [disabled]="!isValid()"
                class="btn btn-action float-right"
                appNotification
                #notification="notification"
                (click)="saveOrganization()">{{'Save' | translate}}
        </button>

        <button type="submit"
                id="cancel_button"
                class="btn btn-link cancel float-right"
                (click)="back()">{{'Cancel' | translate}}
        </button>
      </div>

      <app-organization-details #details="details"
                                id="organization_details"
                                [organization]="organization" 
                                [editing]="true"></app-organization-details>

      <h3 class="mt-4" translate>Admin users</h3>

      <p *ngIf="organizationAdminUsers.length === 0" translate>No admin users yet</p>
      <ul *ngIf="organizationAdminUsers.length > 0">
        <li *ngFor="let user of organizationAdminUsers" id="{{user.email + '_org_admin_user_item'}}">{{user.name}}</li>
      </ul>

      <button type="button"
              id="add_user_button"
              class="btn btn-action"
              (click)="addUser()" translate>Add user</button>

    </div>
  `,
  styleUrls: ['./new-organization.component.scss']
})
export class NewOrganizationComponent {

  organization = OrganizationDetails.empty();
  organizationAdminUsers: User[] = [];
  successfullySaved = false;

  @ViewChild('notification') notification: NotificationDirective;
  @ViewChild('details') details: OrganizationDetailsComponent;

  constructor(locationService: LocationService,
              private searchModal: SearchUserModalService,
              private apiService: ApiService,
              private router: Router,
              private translateService: TranslateService) {

    locationService.atAddNewOrganization();
  }

  get organizationAdminEmails(): string[] {
    return this.organizationAdminUsers.map(user => user.email);
  }

  isValid() {
    return this.details.isValid() && this.organizationAdminUsers.length > 0;
  }

  hasChanges() {
    return !this.successfullySaved && (this.details.hasChanges() || this.organizationAdminUsers.length > 0);
  }

  addUser() {
    this.searchModal.open(this.organizationAdminEmails)
      .then(user => this.organizationAdminUsers.push(user), ignoreModalClose);
  }

  saveOrganization() {
      this.apiService.createOrganization(this.organization, this.organizationAdminEmails).subscribe( {
        next: id => {
          this.successfullySaved = true;
          this.router.navigate(['/organization', id]);
        },
        error: () => this.notification.showFailure(this.translateService.instant('Save failed'), 3000, 'left'),
      });
  }

  back() {
    this.router.navigate(['/']);
  }
}
