import { afterNextRender, Directive, ElementRef, inject } from '@angular/core';

/**
 * Moves keyboard and screen-reader focus to the host (a step heading) when it first appears, so the user
 * lands on each new step. `tabindex="-1"` makes a heading focusable without adding it to the Tab order.
 */
@Directive({
  selector: '[appFocusOnRender]',
  host: { tabindex: '-1' },
})
export class FocusOnRender {
  constructor() {
    const host = inject<ElementRef<HTMLElement>>(ElementRef);
    afterNextRender(() => host.nativeElement.focus());
  }
}
