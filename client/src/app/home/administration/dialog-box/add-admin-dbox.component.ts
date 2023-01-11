import { Component, Inject } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { ActionsEnum } from 'src/app/models/actions.model';
import { Person } from 'src/app/models/person.model';

@Component({
  selector: 'app-add-admin-dialog-box',
  templateUrl: './add-admin-dbox.component.html',
  styleUrls: ['./add-admin-dbox.component.css']
})
export class AddAdminDBoxComponent {
  local_users: Person[];
  local_selected_user: Person;
  private actionsEnum = ActionsEnum;

  constructor(
    public dialogRef: MatDialogRef<AddAdminDBoxComponent>,
    @Inject(MAT_DIALOG_DATA) public users: Person[]) {
      this.local_users = users;
      this.local_selected_user = users[0];
    }

  /**
   * Update the selected user
   * @param event 
   */
  public changeSelectedUser(event: any) {
    this.local_selected_user = event.value;
  }

  /**
   * Do the action
   */
  doAction(){
    this.dialogRef.close({
      event: this.actionsEnum[this.actionsEnum.addAdmin],
      data: this.local_selected_user
    });
  }

  /**
   * Cancel the action
   */
  closeDialog(){
    this.dialogRef.close({
      event: this.actionsEnum[this.actionsEnum.cancel]
    });
  }
}
