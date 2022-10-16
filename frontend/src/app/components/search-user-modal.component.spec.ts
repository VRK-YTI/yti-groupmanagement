import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { SearchUserModalComponent } from './search-user-modal.component';

describe('SearchUserModalComponent', () => {
  let component: SearchUserModalComponent;
  let fixture: ComponentFixture<SearchUserModalComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ SearchUserModalComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SearchUserModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
