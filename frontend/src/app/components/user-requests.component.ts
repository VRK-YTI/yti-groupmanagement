import { Component } from '@angular/core';
import { ApiService } from '../services/api.service';
import { UserRequestWithOrganization } from '../apina';
import { remove, comparingPrimitive } from '@vrk-yti/yti-common-ui';

@Component({
  selector: 'app-user-requests',
  template: `
    <div *ngIf="userRequests.length > 0">

      <h2 translate>Access requests</h2>

      <table class="table table-striped">
        <tbody>
        <tr *ngFor="let request of userRequests">
          <td >{{request.fullName}}</td>
          <td>{{request.email}}</td>
          <td>{{request.organizationName | translateValue }}</td>
          <td>{{request.role | translate}}</td>
          <td class="actions">

            <button type="button"
                    id="{{request.email + '_decline_request_button'}}"
                    class="btn btn-link"
                    (click)="declineRequest(request)">
              <i class="fa fa-trash"></i>
              <span translate>Decline</span>
            </button>

            <button type="button"
                    id="{{request.email + '_accept_request_button'}}"
                    class="btn btn-action"
                    (click)="acceptRequest(request)" translate>Accept</button>
          </td>
        </tr>
        </tbody>
      </table>

    </div>
  `,
  styleUrls: ['./user-requests.component.scss']
})
export class UserRequestsComponent {

  userRequests: UserRequestWithOrganization[] = [];

  constructor(private apiService: ApiService) {

    this.apiService.getAllUserRequests().subscribe( requests => {
      requests.sort(
        comparingPrimitive<UserRequestWithOrganization>(r => r.lastName)
          .andThen(comparingPrimitive<UserRequestWithOrganization>(r => r.firstName))
      );
      this.userRequests = requests;
    });
  }

  declineRequest(userRequest: UserRequestWithOrganization) {
    this.apiService.declineRequest(userRequest.id).subscribe(() =>
      remove(this.userRequests, userRequest));
  }

  acceptRequest(userRequest: UserRequestWithOrganization) {
    this.apiService.acceptRequest(userRequest.id).subscribe(() =>
      remove(this.userRequests, userRequest));
  }
}
