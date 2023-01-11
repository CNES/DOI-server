import { Component, Inject } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { ActionsEnum } from 'src/app/models/actions.model';
import { Person } from 'src/app/models/person.model';

@Component({
  selector: 'app-remove-project-user-dbox',
  templateUrl: './remove-project-user-dbox.component.html',
  styleUrls: ['./remove-project-user-dbox.component.css']
})
export class RemoveProjectUserDBoxComponent {
  private actionsEnum = ActionsEnum;
  local_user: Person;
  local_project_name: string;

  constructor(
    public dialogRef: MatDialogRef<RemoveProjectUserDBoxComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any) {
    this.local_user = data.user;
    this.local_project_name = data.project.projectname;
  }

  /**
   * Do the action
   */
  doAction(){
    this.dialogRef.close({
      event: this.actionsEnum[this.actionsEnum.removeProjectUser],
      data:this.local_user
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
