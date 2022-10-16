import { Localizable } from '@vrk-yti/yti-common-ui';
import { Organization, OrganizationListItem, UUID } from '../apina';

export class OrganizationDetails {

  constructor(public url: string,
              public nameFi: string,
              public nameEn: string,
              public nameSv: string,
              public descriptionFi: string,
              public descriptionEn: string,
              public descriptionSv: string,
              public removed: boolean,
              public parentId: UUID,
              public childOrganizations: OrganizationListItem[]) {
  }

  static empty() {
    return new OrganizationDetails('', '', '', '', '', '', '', false, '', []);
  }

  static emptyChildOrganization(parentId: UUID) {
    return new OrganizationDetails('', '', '', '', '', '', '', false, parentId, []);
  }

  static fromOrganization(model: Organization, childOrganizations: OrganizationListItem[]) {
    return new OrganizationDetails(
      model.url,
      model.nameFi,
      model.nameEn,
      model.nameSv,
      model.descriptionFi,
      model.descriptionEn,
      model.descriptionSv,
      model.removed,
      model.parentId,
      childOrganizations
    );
  }

  get name(): Localizable {
    return {
      'fi': this.nameFi,
      'en': this.nameEn,
      'sv': this.nameSv
    };
  }

  get description(): Localizable {
    return {
      'fi': this.descriptionFi,
      'en': this.descriptionEn,
      'sv': this.descriptionSv
    };
  }

  clone() {
    return new OrganizationDetails(
      this.url,
      this.nameFi,
      this.nameEn,
      this.nameSv,
      this.descriptionFi,
      this.descriptionEn,
      this.descriptionSv,
      this.removed,
      this.parentId,
      this.childOrganizations
    );
  }
}
