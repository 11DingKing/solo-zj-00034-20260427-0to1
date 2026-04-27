import { Injectable } from "@angular/core";
import {
  CanActivate,
  Router,
  ActivatedRouteSnapshot,
  RouterStateSnapshot,
} from "@angular/router";
import { AuthService } from "./auth.service";

@Injectable({
  providedIn: "root",
})
export class AuthGuard implements CanActivate {
  constructor(
    private authService: AuthService,
    private router: Router,
  ) {}

  canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot,
  ): boolean {
    const currentUser = this.authService.currentUserValue;
    const roles = route.data["roles"] as string[];

    if (!this.authService.isAuthenticated()) {
      this.router.navigate(["/login"], {
        queryParams: { returnUrl: state.url },
      });
      return false;
    }

    if (roles && roles.length > 0) {
      const hasRole = roles.some((role) => {
        switch (role) {
          case "PATIENT":
            return this.authService.isPatient();
          case "DOCTOR":
            return this.authService.isDoctor();
          case "ADMIN":
            return this.authService.isAdmin();
          default:
            return false;
        }
      });

      if (!hasRole) {
        this.router.navigate(["/access-denied"]);
        return false;
      }
    }

    return true;
  }
}
