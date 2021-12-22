import { Component, Input, ViewChild } from '@angular/core';
import { OrganizationDetails } from '../entities/organization-details';
import { AuthorizationManager } from '../services/authorization-manager.service';
import { NgForm } from '@angular/forms';

@Component({
  selector: 'app-organization-details',
  exportAs: 'details',
  template: `
    <form #form="ngForm">
      <div *ngIf="parentOrganization" class="row">
        <div class="col-md-4">
          <div class="form-group section">
            <p class="form-control-static"><span translate>Main organization</span>: {{parentOrganization}}</p>
          </div>
        </div>
      </div>
      <div class="row">
        <div class="col-md-4">

          <h4 translate>In Finnish</h4>
          <div class="form-group section">

            <label for="name_fi" translate>Name</label>
            <input *ngIf="editing"
                   type="text"
                   class="form-control"
                   id="name_fi"
                   name="name_fi"
                   [(ngModel)]="organization.nameFi">
            <p *ngIf="!editing" class="form-control-static">{{organization.nameFi}}</p>

            <label for="description_fi" translate>Description</label>
            <textarea *ngIf="editing"
                      id="description_fi"
                      name="description_fi"
                      class="form-control"
                      rows="4"
                      [(ngModel)]="organization.descriptionFi"></textarea>
            <p *ngIf="!editing" class="form-control-static">{{organization.descriptionFi}}</p>

          </div>

        </div>

        <div class="col-md-4">

          <h4 translate>In English</h4>

          <div class="form-group section">

            <label for="name_en" translate>Name</label>
            <input *ngIf="editing"
                   type="text"
                   class="form-control"
                   id="name_en"
                   name="name_en"
                   [(ngModel)]="organization.nameEn">
            <p *ngIf="!editing" class="form-control-static">{{organization.nameEn}}</p>

            <label for="description_en" translate>Description</label>
            <textarea *ngIf="editing"
                      id="description_en"
                      name="description_en"
                      class="form-control"
                      rows="4"
                      [(ngModel)]="organization.descriptionEn"></textarea>
            <p *ngIf="!editing" class="form-control-static">{{organization.descriptionEn}}</p>

          </div>
        </div>

        <div class="col-md-4">

          <h4 translate>In Swedish</h4>

          <div class="form-group section">

            <label for="name_sv" translate>Name</label>
            <input *ngIf="editing"
                   type="text"
                   class="form-control"
                   id="name_sv"
                   name="name_sv"
                   [(ngModel)]="organization.nameSv">
            <p *ngIf="!editing" class="form-control-static">{{organization.nameSv}}</p>

            <label for="description_sv" translate>Description</label>
            <textarea *ngIf="editing"
                      id="description_sv"
                      name="description_sv"
                      class="form-control"
                      rows="4"
                      [(ngModel)]="organization.descriptionSv"></textarea>
            <p *ngIf="!editing" class="form-control-static">{{organization.descriptionSv}}</p>

          </div>
        </div>
      </div>

      <div class="row">
        <div class="col-12">
          <div class="form-group">
            <label for="url_input">Url</label>
            <input *ngIf="editing"
                   type="text"
                   class="form-control"
                   id="url_input"
                   name="url_input"
                   [(ngModel)]="organization.url">
            <p *ngIf="!editing" class="form-control-static">{{organization.url}}</p>
          </div>
        </div>
        <div class="col-12">

          <div class="form-check" *ngIf="editing">
            <label class="form-check-label">
              <input class="form-check-input"
                     id="organization_removed_checkbox"
                     type="checkbox"
                     name="removed"
                     [(ngModel)]="organization.removed" />
              {{'Removed' | translate}}
            </label>
          </div>

          <div *ngIf="!editing && organization.removed"
               class="alert alert-danger d-inline-block"
               role="alert"
               translate>Removed</div>
        </div>
      </div>
    </form>
  `,
  styleUrls: ['./organization-details.component.scss']
})
export class OrganizationDetailsComponent {

  @Input()
  organization: OrganizationDetails;

  @Input()
  editing: boolean;

  @Input()
  parentOrganization: string;

  @ViewChild('form') form: NgForm;

  constructor(private authorizationManager: AuthorizationManager) {
  }

  hasChanges() {
    return this.form.dirty;
  }

  reset() {
    // FIXME: no idea why this timeout hack is needed
    setTimeout(() => this.form.resetForm());
  }

  canRemoveOrganization(): boolean {
    return this.authorizationManager.canRemoveOrganization();
  }

  isValid() {
    return this.form.valid;
  }
}
