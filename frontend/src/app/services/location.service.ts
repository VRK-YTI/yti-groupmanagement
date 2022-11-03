import { Injectable, OnDestroy } from '@angular/core';
import { Localizable } from '@vrk-yti/yti-common-ui';
import { Subject, Subscription } from 'rxjs';
import { OrganizationDetails } from '../entities/organization-details';
import { Title } from "@angular/platform-browser";
import { TranslateService } from "@ngx-translate/core";
import { ConfigurationService } from "./configuration.service";

export interface Location {
  localizationKey?: string;
  label?: Localizable;
  route?: string[];
}

const frontPage = { localizationKey: 'Front page', route: [''] };
const newOrganization = { localizationKey: 'Add new organization' };

@Injectable()
export class LocationService implements OnDestroy {

  location = new Subject<Location[]>();
  private titleTranslationSubscription: Subscription;

  constructor(private translateService: TranslateService,
              private configurationService: ConfigurationService,
              private titleService: Title) {
    this.titleTranslationSubscription = this.translateService.stream('Interoperability platform\'s user right management').subscribe(value => {
      this.titleService.setTitle(this.configurationService.getEnvironmentIdentifier('prefix') + value);
    });
  }

  private changeLocation(location: Location[]): void {
    location.unshift(frontPage);
    this.location.next(location);
  }

  atFrontPage(): void {
    this.changeLocation([]);
  }

  atAddNewOrganization(): void {
    this.changeLocation([newOrganization]);
  }

  atOrganization(organization: OrganizationDetails): void {
    this.changeLocation([{ label: organization.name }]);
  }

  atUserDetails() {
    this.changeLocation([{ localizationKey: 'User details' }]);
  }

  atInformationAboutService(): void {
    this.changeLocation([{
      localizationKey: 'Information about the service',
        route: ['information']
    }]);
  }

  ngOnDestroy() {
    this.titleTranslationSubscription.unsubscribe();
  }

}
