import {
  AfterViewInit,
  Component, ElementRef, Injectable, Input, OnInit, Renderer,
  ViewChild
} from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { Observable, BehaviorSubject, combineLatest } from 'rxjs';
import { User } from '../entities/user';
import { ApiService } from '../services/api.service';
import { ModalService } from 'yti-common-ui/services/modal.service';
import { map } from 'rxjs/operators';

@Injectable()
export class SearchUserModalService {

  constructor(private modalService: ModalService) {
  }

  open(excludedUserEmails: string[]): Promise<User> {
    const modalRef = this.modalService.open(SearchUserModalComponent, { size: 'sm' });
    const instance = modalRef.componentInstance as SearchUserModalComponent;
    instance.excludedUserEmails = excludedUserEmails;
    return modalRef.result;
  }
}

@Component({
  selector: 'app-search-modal',
  template: `
    <div class="modal-header">
      <h4 class="modal-title">
        <a><i class="fa fa-times" id="search_modal_cancel_link" (click)="cancel()"></i></a>
        <span translate>Select user</span>
      </h4>
    </div>

    <div class="modal-body full-height">
      <div class="row mb-2">
        <div class="col-12">
          <div class="input-group input-group-lg input-group-search">
            <input #searchInput
                   id="search_user_input"
                   type="text"
                   class="form-control"
                   placeholder="{{'Search user' | translate}}"
                   [(ngModel)]="search"/>
          </div>
        </div>
      </div>

      <div class="row full-height">
        <div class="col-12">
          <div class="content-box">
            <div class="search-results">
              <div *ngFor="let user of searchResults$ | async; let last = last"
                   class="search-result"
                   id="{{'search_results_user_' + user.email}}"
                   (click)="selectUser(user)">

                <div class="content" [class.last]="last">
                  <span class="title">{{user.name}}</span>
                  <span class="body">{{user.email}}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="modal-footer">

      <button type="button"
              id="cancel_button"
              class="btn btn-link cancel"
              (click)="cancel()">{{'Cancel' | translate}}
      </button>

    </div>
  `,
  styleUrls: ['./search-user-modal.component.scss']
})

export class SearchUserModalComponent implements OnInit, AfterViewInit {

  @ViewChild('searchInput') searchInput: ElementRef;

  @Input()
  excludedUserEmails: string[];

  search$  = new BehaviorSubject('');
  searchResults$: Observable<User[]>;

  constructor(public modal: NgbActiveModal,
              private apiService: ApiService,
              private renderer: Renderer) {
  }

  ngOnInit() {
    this.searchResults$ =
      combineLatest(this.search$, this.apiService.getUsers()).pipe(map(([search, users]) => {

        const isUserAddable = (user: User) => {
          return !this.isExcluded(user) && (!search || user.name.toLowerCase().indexOf(search.toLowerCase()) !== -1
                                              || user.email.toLowerCase().indexOf(this.search.toLowerCase()) !== -1);
        };

        return users.filter(user => isUserAddable(user));
      }));
  }

  isExcluded(user: User) {
    for (const excludedEmail of this.excludedUserEmails) {
      if (user.email === excludedEmail) {
        return true;
      }
    }
    return false;
  }

  selectUser(user: User) {
    this.modal.close(user);
  }

  ngAfterViewInit() {
    this.renderer.invokeElementMethod(this.searchInput.nativeElement, 'focus');
  }

  get search() {
    return this.search$.getValue();
  }

  set search(value: string) {
    this.search$.next(value);
  }

  cancel() {
    this.modal.dismiss('cancel');
  }
}
