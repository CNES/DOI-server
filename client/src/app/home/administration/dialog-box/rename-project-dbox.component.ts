import { Component, Inject } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { ActionsEnum } from 'src/app/models/actions.model';
import { Project } from 'src/app/models/projects.model';

@Component({
  selector: 'app-rename-project-dialog-box',
  templateUrl: './rename-project-dbox.component.html',
  styleUrls: ['./rename-project-dbox.component.css']
})
export class RenameProjectDBoxComponent {
  local_project: Project;
  local_newName: string;
  private actionsEnum = ActionsEnum;

  constructor(
    public dialogRef: MatDialogRef<RenameProjectDBoxComponent>,
    @Inject(MAT_DIALOG_DATA) public proj: Project) {
      this.local_project = proj;
      this.local_newName = proj.projectname;
    }

  /**
   * Do the action
   */
  doAction(){
    this.local_project.projectname = this.local_newName;

    this.dialogRef.close({
      event: this.actionsEnum[this.actionsEnum.renameProject],
      data: this.local_project
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
