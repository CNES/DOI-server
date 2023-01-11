import { Component, Inject } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { ActionsEnum } from 'src/app/models/actions.model';
import { Person } from 'src/app/models/person.model';

@Component({
  selector: 'app-remove-admin-dialog-box',
  templateUrl: './remove-admin-dbox.component.html',
  styleUrls: ['./remove-admin-dbox.component.css']
})
export class RemoveAdminDBoxComponent {
  local_user: Person;
  private actionsEnum = ActionsEnum;

  constructor(
    public dialogRef: MatDialogRef<RemoveAdminDBoxComponent>,
    @Inject(MAT_DIALOG_DATA) public user: Person) {
      this.local_user = user;
    }

  /**
   * Do the action
   */
  doAction(){
    this.dialogRef.close({
      event: this.actionsEnum[this.actionsEnum.removeAdmin],
      data: this.local_user
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
