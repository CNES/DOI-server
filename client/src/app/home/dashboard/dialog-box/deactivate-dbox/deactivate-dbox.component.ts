import { Component, Inject } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';

@Component({
  selector: 'app-deactivate-dbox',
  templateUrl: './deactivate-dbox.component.html',
  styleUrls: ['./deactivate-dbox.component.css']
})
export class DeactivateDboxComponent {

  constructor(
    public dialogRef: MatDialogRef<DeactivateDboxComponent>,
    @Inject(MAT_DIALOG_DATA) public data: DialogData) {}

  onNoClick(): void {
    this.dialogRef.close();
  }
}

export interface DialogData {
  doi: string;
  confirmation: boolean;
}
