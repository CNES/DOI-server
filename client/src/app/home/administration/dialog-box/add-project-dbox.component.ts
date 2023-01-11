import { Component } from '@angular/core';
import { MatDialogRef } from '@angular/material/dialog';
import { ActionsEnum } from 'src/app/models/actions.model';

@Component({
  selector: 'app-add-project-dialog-box',
  templateUrl: './add-project-dbox.component.html',
  styleUrls: ['./add-project-dbox.component.css']
})
export class AddProjectDBoxComponent {
  local_newName: string = "";
  private actionsEnum = ActionsEnum;

  constructor(
    public dialogRef: MatDialogRef<AddProjectDBoxComponent>) {}

  /**
   * Do the action
   */
  doAction(){
    this.dialogRef.close({
      event: this.actionsEnum[this.actionsEnum.addProject],
      data: this.local_newName
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
