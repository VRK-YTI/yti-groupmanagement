import { Component, ElementRef, ViewChild } from '@angular/core';
import { LocationService } from '../services/location.service';
import { AuthorizationManager } from '../services/authorization-manager.service';
import { NgbNav, NgbNavChangeEvent } from '@ng-bootstrap/ng-bootstrap';

@Component({
  selector: 'app-frontpage',
  styleUrls: ['./frontpage.component.scss'],
  template: `
    <div class="content-box">

      <app-user-requests></app-user-requests>

      <ul ngbNav #nav="ngbNav" (navChange)="onNavChange($event)">
        <li ngbNavItem="organizations_tab" id="organizations_tab">
          <a ngbNavLink>
            <span translate>ORGANIZATIONS</span>
          </a>

          <ng-template ngbNavContent>
            <app-organizations></app-organizations>
          </ng-template>
        </li>

        <li ngbNavItem="users_tab" id="users_tab" *ngIf="canBrowseUsers()">
          <a ngbNavLink>
            <span translate>USERS</span>
          </a>

          <ng-template ngbNavContent>
            <app-users></app-users>
          </ng-template>
        </li>

      </ul>

      <div [ngbNavOutlet]="nav"></div>

    </div>
  `
})

export class FrontpageComponent {
  @ViewChild('nav') nav: ElementRef<NgbNav>;


  constructor(locationService: LocationService,
              private authorizationManager: AuthorizationManager) {
    locationService.atFrontPage();
  }

  canBrowseUsers(): boolean {
    return this.authorizationManager.canBrowseUsers();
  }

  onNavChange(event: NgbNavChangeEvent) {
    this.nav.nativeElement.activeId = event.nextId;
  }
}
