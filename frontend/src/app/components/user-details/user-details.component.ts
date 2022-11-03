import { Component, ElementRef, OnInit, ViewChild } from '@angular/core';
import { Router } from '@angular/router';
import { LanguageService } from '../../services/language.service';
import { LocationService } from '../../services/location.service';
import { UserService } from '@vrk-yti/yti-common-ui';
import { NgbNavChangeEvent, NgbNav } from '@ng-bootstrap/ng-bootstrap';
import { MessagingResource } from '../../entities-messaging/messaging-resource';
import { BehaviorSubject } from 'rxjs';
import { MessagingService } from '../../services/messaging-service';
import { ConfigurationService } from '../../services/configuration.service';

@Component({
  selector: 'app-user-details',
  styleUrls: ['./user-details.component.scss'],
  templateUrl: './user-details.component.html',
})
export class UserDetailsComponent implements OnInit {

  @ViewChild('nav') nav: ElementRef<NgbNav>;

  APPLICATION_CODELIST = 'codelist';
  APPLICATION_TERMINOLOGY = 'terminology';
  APPLICATION_DATAMODEL = 'datamodel';
  APPLICATION_COMMENTS = 'comments';

  loading = true;

  messagingResources$ = new BehaviorSubject<Map<string, MessagingResource[]> | null>(null);

  constructor(private router: Router,
              private userService: UserService,
              private languageService: LanguageService,
              locationService: LocationService,
              private messagingService: MessagingService,
              private configurationService: ConfigurationService) {

    locationService.atUserDetails();
  }

  ngOnInit() {

    if (this.configurationService.isMessagingEnabled && this.userService.isLoggedIn()) {
      this.getUserSubscriptionData();
    } else {
      this.loading = false;
    }
  }

  getUserSubscriptionData() {

    this.loading = true;

    this.messagingService.getMessagingUserData().subscribe(messagingUserData => {
      if (messagingUserData) {
        const resources = new Map<string, MessagingResource[]>();
        const codelistMessagingResources: MessagingResource[] = [];
        const datamodelMessagingResources: MessagingResource[] = [];
        const terminologyMessagingResources: MessagingResource[] = [];
        const commentsMessagingResources: MessagingResource[] = [];

        messagingUserData.resources.forEach(resource => {
          if (resource.application === this.APPLICATION_CODELIST) {
            codelistMessagingResources.push(resource);
          } else if (resource.application === this.APPLICATION_DATAMODEL) {
            datamodelMessagingResources.push(resource);
          } else if (resource.application === this.APPLICATION_TERMINOLOGY) {
            terminologyMessagingResources.push(resource);
          } else if (resource.application === this.APPLICATION_COMMENTS) {
            commentsMessagingResources.push(resource);
          }
        });
        if (codelistMessagingResources.length > 0) {
          resources.set(this.APPLICATION_CODELIST, codelistMessagingResources);
        }
        if (datamodelMessagingResources.length > 0) {
          resources.set(this.APPLICATION_DATAMODEL, datamodelMessagingResources);
        }
        if (terminologyMessagingResources.length > 0) {
          resources.set(this.APPLICATION_TERMINOLOGY, terminologyMessagingResources);
        }
        if (commentsMessagingResources.length > 0) {
          resources.set(this.APPLICATION_COMMENTS, commentsMessagingResources);
        }
        if (resources.size > 0) {
          this.messagingResources = resources;
        } else {
          this.messagingResources = null;
        }
      } else {
        this.messagingResources = null;
      }
      this.loading = false;
    });
  }

  onNavChange(event: NgbNavChangeEvent) {
    this.nav.nativeElement.activeId = event.nextId;
    if (event.nextId === 'user_details_info_tab') {
      this.getUserSubscriptionData();
    }
  }

  get messagingResources(): Map<string, MessagingResource[]> | null {

    return this.messagingResources$.getValue();
  }

  set messagingResources(value: Map<string, MessagingResource[]> | null) {

    this.messagingResources$.next(value);
  }

  get user() {
    return this.userService.user;
  }
}
