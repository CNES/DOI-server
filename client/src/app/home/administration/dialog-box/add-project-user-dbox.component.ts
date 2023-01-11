import { Component, Inject } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { ActionsEnum } from 'src/app/models/actions.model';
import { Person } from 'src/app/models/person.model';
import { Project } from 'src/app/models/projects.model';

@Component({
  selector: 'app-add-user-project-dialog-box',
  templateUrl: './add-project-user-dbox.component.html',
  styleUrls: ['./add-admin-dbox.component.css']
})
export class AddProjectUserDBoxComponent {
  project: Project
  local_users: Person[];
  local_selected_user: Person;
  private actionsEnum = ActionsEnum;

  constructor(
    public dialogRef: MatDialogRef<AddProjectUserDBoxComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any) {
      this.project = data.project;
      this.local_users = data.users;
      this.local_selected_user = data.users[0];
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
      event: this.actionsEnum[this.actionsEnum.addProjectUser],
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
