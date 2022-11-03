import { Component, OnDestroy } from '@angular/core';
import { OrganizationListItem } from '../apina';
import { Router } from '@angular/router';
import { AuthorizationManager } from '../services/authorization-manager.service';
import { ApiService } from '../services/api.service';
import { LanguageService } from '../services/language.service';
import { comparingLocalizable, matches } from '@vrk-yti/yti-common-ui';
import { BehaviorSubject, Subscription, combineLatest } from 'rxjs';

@Component({
  selector: 'app-organizations',
  template: `
    <app-ajax-loading-indicator *ngIf="loading"></app-ajax-loading-indicator>

    <div *ngIf="!loading">

      <h1 translate>Organizations</h1>

      <div class="top-actions">

        <div class="input-group input-group-lg input-group-search float-left">
          <input class="form-control"
                 id="search_organization_input"
                 type="text"
                 [(ngModel)]="search"
                 placeholder="{{'Search organization' | translate}}"/>
        </div>

        <div>
          <input #showRemovedCheckBox id="showRemovedCheckBox" id="show_removed_organizations_checkbox" type="checkbox" name="showRemovedCheckBox"
                 (change)="showRemovedChanged()"/>
                 <span id="show_removed_text_span" translate>Show removed organizations only</span>
        </div>

        <button class="btn btn-action float-right" id="add_new_organization_button" (click)="addOrganization()"
                *ngIf="canCreateOrganization()">
          <span translate>Add new organization</span>
        </button>
      </div>

      <div *ngFor="let organization of filteredOrganizations"
           id="{{'organization_list_item_' + organization.id}}"
           class="organization"
           [class.viewable]="canViewOrganization(organization)"
           (click)="navigate(organization)">

        <span class="name">{{organization.name | translateValue}}</span>
      </div>
    </div>
  `,
  styleUrls: ['./organizations.component.scss']
})

export class OrganizationsComponent implements OnDestroy {

  search$ = new BehaviorSubject('');
  filteredOrganizations: OrganizationListItem[];

  subscription: Subscription;
  showRemovedCheckBox: boolean | false;


  constructor(private apiService: ApiService,
              private authorizationManager: AuthorizationManager,
              private router: Router,
              private languageService: LanguageService) {

    this.subscription = combineLatest(apiService.getOrganizationListOpt(false), this.search$, languageService.language$)
      .subscribe(([organizations, search]) => {
        organizations.sort(comparingLocalizable<OrganizationListItem>(languageService, org => org.name));
        this.filteredOrganizations = organizations.filter(org => matches(languageService.translate(org.name), search));
      });
  }

  get loading(): boolean {
    return this.filteredOrganizations == null;
  }

  canViewOrganization(organization: OrganizationListItem): boolean {
    return this.authorizationManager.canViewOrganization(organization.id);
  }

  canCreateOrganization(): boolean {
    return this.authorizationManager.canCreateOrganization();
  }

  get search(): string {
    return this.search$.getValue();
  }

  set search(value: string) {
    this.search$.next(value);
  }

  navigate(organization: OrganizationListItem) {
    if (this.canViewOrganization(organization)) {
      this.router.navigate(['/organization', organization.id]);
    }
  }

  showRemovedChanged() {
      this.showRemovedCheckBox = !this.showRemovedCheckBox;
      this.subscription.unsubscribe();
      this.subscription = combineLatest(this.apiService.getOrganizationListOpt(this.showRemovedCheckBox), this.search$, this.languageService.language$)
        .subscribe(([organizations, search]) => {
        organizations.sort(comparingLocalizable<OrganizationListItem>(this.languageService, org => org.name));
        this.filteredOrganizations = organizations.filter(org => matches(this. languageService.translate(org.name), search));
      });
  }

  addOrganization() {
    this.router.navigate(['/newOrganization']);
  }

  ngOnDestroy() {
    this.subscription.unsubscribe();
  }
}

