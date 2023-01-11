import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormControl, FormGroup } from '@angular/forms';
import { AlertService } from 'src/app/services/alert.service';
import { AuthService } from 'src/app/services/auth.service';

@Component({
  selector: 'app-creation-token',
  templateUrl: './creation-token.component.html',
  styleUrls: ['./creation-token.component.css']
})
export class CreationTokenComponent implements OnInit {

  createTokenForm!: FormGroup;

  constructor(public fb: FormBuilder,
    private authService: AuthService,
    private alertService: AlertService) { }

  ngOnInit(): void {
    this.alertService.clear();

    this.createTokenForm = this.fb.group({
      strToken: new FormControl({ value: '', disabled: true })
    });
  }

  /**
   * Set the token
   */
  public createToken(): void {
    let sub = this.authService.getToken().subscribe(
      data => {
        this.alertService.clear();
        this.alertService.success('Token créée');
        this.createTokenForm.get("strToken")?.setValue(data)
        sub.unsubscribe();
      },
      error => {
        if (error && error.statusText == "Unknown Error") {
          this.alertService.error('Une erreur est survenue durant la création du token, impossible de contacter le serveur');
        }
      }
    );
  }

  /**
   * Confirm that the token is copied
   */
  public tokenCopied(): void {
    this.alertService.clear();
    this.alertService.success("Token copié dans le presse papier")
  }
}
