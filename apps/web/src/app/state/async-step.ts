import { computed, signal } from '@angular/core';
import { firstValueFrom, Observable } from 'rxjs';

import { errorMessage } from '../api/error-message';

/** One request-backed step: its result, whether it's loading, and a user-facing error. */
export class AsyncStep<T> {
  readonly value = signal<T | undefined>(undefined);
  readonly loading = signal(false);
  readonly error = signal<string | undefined>(undefined);
  /** True once the step was run, so its section should be on screen. */
  readonly started = computed(() => this.loading() || !!this.value() || !!this.error());

  async run(request: () => Observable<T>): Promise<void> {
    this.value.set(undefined);
    this.error.set(undefined);
    this.loading.set(true);
    try {
      this.value.set(await firstValueFrom(request()));
    } catch (error) {
      this.error.set(errorMessage(error));
    } finally {
      this.loading.set(false);
    }
  }

  reset(): void {
    this.value.set(undefined);
    this.error.set(undefined);
    this.loading.set(false);
  }
}
