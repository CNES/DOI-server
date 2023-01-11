import { Component, Inject } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { ActionsEnum } from 'src/app/models/actions.model';
import { Project } from 'src/app/models/projects.model';

@Component({
  selector: 'app-remove-project-dbox',
  templateUrl: './remove-project-dbox.component.html',
  styleUrls: ['./remove-project-dbox.component.css']
})
export class RemoveProjectDBoxComponent {
  private actionsEnum = ActionsEnum;
  local_project: Project;

  constructor(
    public dialogRef: MatDialogRef<RemoveProjectDBoxComponent>,
    @Inject(MAT_DIALOG_DATA) public proj: Project) {
    this.local_project = proj;
  }

  /**
   * Do the action
   */
  doAction(){
    this.dialogRef.close({
      event: this.actionsEnum[this.actionsEnum.removeProject],
      data:this.local_project
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
