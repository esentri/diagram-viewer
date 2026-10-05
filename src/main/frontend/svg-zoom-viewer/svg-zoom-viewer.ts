import { css, LitElement, html, nothing } from "lit";
import { property, state } from "lit/decorators.js";

export class SvgZoomViewer extends LitElement {
  static styles = css`
    :host {
      display: block;
      position: relative;
      width: 100%;
      height: 100%;
      overflow: hidden;
    }

    #viewport {
      width: 100%;
      height: 100%;
      overflow: hidden;
      cursor: grab;
      touch-action: none;
      user-select: none;
      -webkit-user-select: none;
    }

    #viewport.dragging {
      cursor: grabbing;
    }

    #transform-layer {
      transform-origin: 0 0;
      will-change: transform;
      display: inline-block;
    }

    #svg-image {
      display: block;
      max-width: none;
      max-height: none;
      -webkit-user-drag: none;
      pointer-events: none;
    }

    #controls {
      position: absolute;
      bottom: 16px;
      right: 16px;
      display: flex;
      flex-direction: column;
      gap: 4px;
      z-index: 10;
    }

    #controls button {
      width: 36px;
      height: 36px;
      border: 1px solid var(--lumo-contrast-20pct, #ccc);
      border-radius: var(--lumo-border-radius-s, 4px);
      background: var(--lumo-base-color, white);
      color: var(--lumo-body-text-color, #333);
      font-size: 18px;
      line-height: 1;
      cursor: pointer;
      display: flex;
      align-items: center;
      justify-content: center;
      box-shadow: 0 1px 3px rgba(0, 0, 0, 0.15);
      transition: background-color 0.15s;
    }

    #controls button:hover {
      background: var(--lumo-contrast-5pct, #f0f0f0);
    }

    #controls button:active {
      background: var(--lumo-contrast-10pct, #e0e0e0);
    }
  `;

  @property({ type: String, reflect: true }) src = "";
  @property({ type: Boolean }) draggable = true;
  @property({ type: Boolean }) wheelable = true;
  @property({ type: Boolean }) pinchable = true;
  @property({ type: Boolean }) bounds = false;
  @property({ type: Number }) minScale = 0.1;
  @property({ type: Number }) maxScale = 20;
  @property({ type: Number }) zoomStep = 0.1;
  @property({ type: Boolean }) showControls = true;

  @state() private _scale = 1;
  @state() private _translateX = 0;
  @state() private _translateY = 0;

  private _isDragging = false;
  private _dragStartX = 0;
  private _dragStartY = 0;
  private _lastTranslateX = 0;
  private _lastTranslateY = 0;

  private _isPinching = false;
  private _initialPinchDistance = 0;
  private _initialPinchScale = 1;
  private _initialPinchMidX = 0;
  private _initialPinchMidY = 0;
  private _lastPinchTranslateX = 0;
  private _lastPinchTranslateY = 0;

  /** the smallest scale: below minScale if a large diagram needs it to fit into the viewport */
  private _effectiveMinScale = this.minScale;
  /** whether the user zoomed or moved the diagram since it was fitted into the viewport */
  private _adjustedByUser = false;
  private _resizeObserver?: ResizeObserver;

  private readonly _onMouseMoveBound: (e: MouseEvent) => void;
  private readonly _onMouseUpBound: (e: MouseEvent) => void;

  constructor() {
    super();
    this._onMouseMoveBound = this._onMouseMove.bind(this);
    this._onMouseUpBound = this._onMouseUp.bind(this);
  }

  render() {
    const transform = `translate(${this._translateX}px, ${this._translateY}px) scale(${this._scale})`;

    return html`
      <div
        id="viewport"
        @wheel=${this._onWheel}
        @mousedown=${this._onMouseDown}
        @touchstart=${this._onTouchStart}
        @touchmove=${this._onTouchMove}
        @touchend=${this._onTouchEnd}
      >
        <div id="transform-layer" style="transform: ${transform}">
          <img id="svg-image" .src=${this.src} draggable="false" alt="Diagram" />
        </div>
      </div>
      ${this.showControls
        ? html`
            <div id="controls">
              <button @click=${this._zoomIn} title="Zoom In" aria-label="Zoom In">+</button>
              <button @click=${this._zoomOut} title="Zoom Out" aria-label="Zoom Out">&minus;</button>
              <button @click=${this._resetView} title="Reset View" aria-label="Reset View">&#8634;</button>
            </div>
          `
        : nothing}
    `;
  }

  firstUpdated(): void {
    const img = this.shadowRoot!.getElementById("svg-image") as HTMLImageElement;
    img.addEventListener("load", () => this._centerImage());
    // fit again while the user has not zoomed or moved: the viewport may get its size only after the image is
    // loaded, e.g. in a dialog or a tab, and changes with the window
    this._resizeObserver = new ResizeObserver(() => {
      if (!this._adjustedByUser) {
        this._centerImage();
      }
    });
    this._resizeObserver.observe(this._getViewport()!);
  }

  disconnectedCallback(): void {
    super.disconnectedCallback();
    this._resizeObserver?.disconnect();
    window.removeEventListener("mousemove", this._onMouseMoveBound);
    window.removeEventListener("mouseup", this._onMouseUpBound);
  }

  // --- Mouse wheel zoom (centered on cursor) ---

  private _onWheel(e: WheelEvent): void {
    if (!this.wheelable) return;
    e.preventDefault();

    const rect = this._getViewportRect();
    const cursorX = e.clientX - rect.left;
    const cursorY = e.clientY - rect.top;

    // proportional to the scrolled distance: a mouse wheel notch zooms by about zoomStep, while the many small
    // events of a touchpad (and of its pinch gesture) zoom smoothly instead of a full step each
    const pixels = e.deltaMode === WheelEvent.DOM_DELTA_LINE ? e.deltaY * 16
      : e.deltaMode === WheelEvent.DOM_DELTA_PAGE ? e.deltaY * rect.height : e.deltaY;
    const factor = Math.exp(-pixels * this.zoomStep / 100);
    this._adjustedByUser = true;
    this._applyZoomAtPoint(factor - 1, cursorX, cursorY);
  }

  // --- Mouse drag ---

  private _onMouseDown(e: MouseEvent): void {
    if (!this.draggable || e.button !== 0) return;

    this._isDragging = true;
    this._adjustedByUser = true;
    this._dragStartX = e.clientX;
    this._dragStartY = e.clientY;
    this._lastTranslateX = this._translateX;
    this._lastTranslateY = this._translateY;

    this._getViewport()?.classList.add("dragging");
    window.addEventListener("mousemove", this._onMouseMoveBound);
    window.addEventListener("mouseup", this._onMouseUpBound);
  }

  private _onMouseMove(e: MouseEvent): void {
    if (!this._isDragging) return;
    this._translateX = this._lastTranslateX + (e.clientX - this._dragStartX);
    this._translateY = this._lastTranslateY + (e.clientY - this._dragStartY);
  }

  private _onMouseUp(): void {
    this._isDragging = false;
    this._getViewport()?.classList.remove("dragging");
    window.removeEventListener("mousemove", this._onMouseMoveBound);
    window.removeEventListener("mouseup", this._onMouseUpBound);
  }

  // --- Touch pinch zoom + pan ---

  private _onTouchStart(e: TouchEvent): void {
    if (e.touches.length === 2 && this.pinchable) {
      e.preventDefault();
      this._isPinching = true;
      this._adjustedByUser = true;

      const [t1, t2] = [e.touches[0], e.touches[1]];
      this._initialPinchDistance = this._getTouchDistance(t1, t2);
      this._initialPinchScale = this._scale;
      this._lastPinchTranslateX = this._translateX;
      this._lastPinchTranslateY = this._translateY;

      const rect = this._getViewportRect();
      this._initialPinchMidX = (t1.clientX + t2.clientX) / 2 - rect.left;
      this._initialPinchMidY = (t1.clientY + t2.clientY) / 2 - rect.top;
    } else if (e.touches.length === 1 && this.draggable) {
      this._isDragging = true;
      this._adjustedByUser = true;
      this._dragStartX = e.touches[0].clientX;
      this._dragStartY = e.touches[0].clientY;
      this._lastTranslateX = this._translateX;
      this._lastTranslateY = this._translateY;
    }
  }

  private _onTouchMove(e: TouchEvent): void {
    if (this._isPinching && e.touches.length === 2) {
      e.preventDefault();

      const [t1, t2] = [e.touches[0], e.touches[1]];
      const distance = this._getTouchDistance(t1, t2);
      const scaleRatio = distance / this._initialPinchDistance;
      const newScale = this._clampScale(this._initialPinchScale * scaleRatio);

      const worldX = (this._initialPinchMidX - this._lastPinchTranslateX) / this._initialPinchScale;
      const worldY = (this._initialPinchMidY - this._lastPinchTranslateY) / this._initialPinchScale;
      this._translateX = this._initialPinchMidX - worldX * newScale;
      this._translateY = this._initialPinchMidY - worldY * newScale;
      this._scale = newScale;
    } else if (this._isDragging && e.touches.length === 1) {
      this._translateX = this._lastTranslateX + (e.touches[0].clientX - this._dragStartX);
      this._translateY = this._lastTranslateY + (e.touches[0].clientY - this._dragStartY);
    }
  }

  private _onTouchEnd(e: TouchEvent): void {
    if (e.touches.length < 2) this._isPinching = false;
    if (e.touches.length === 0) this._isDragging = false;
  }

  // --- Zoom controls ---

  private _zoomIn(): void {
    this._adjustedByUser = true;
    this._applyZoomAtCenter(this.zoomStep);
  }

  private _zoomOut(): void {
    this._adjustedByUser = true;
    this._applyZoomAtCenter(-this.zoomStep);
  }

  private _resetView(): void {
    this._centerImage();
  }

  // --- Public API (server-callable) ---

  zoom(ratio: number): void {
    this._adjustedByUser = true;
    this._applyZoomAtCenter(ratio);
  }

  reset(): void {
    this._centerImage();
  }

  moveTo(x: number, y: number): void {
    this._adjustedByUser = true;
    this._translateX = x;
    this._translateY = y;
  }

  // --- Internal helpers ---

  private _applyZoomAtPoint(delta: number, pointX: number, pointY: number): void {
    const newScale = this._clampScale(this._scale + delta * this._scale);
    if (newScale === this._scale) return;

    const worldX = (pointX - this._translateX) / this._scale;
    const worldY = (pointY - this._translateY) / this._scale;
    this._translateX = pointX - worldX * newScale;
    this._translateY = pointY - worldY * newScale;
    this._scale = newScale;
  }

  private _applyZoomAtCenter(delta: number): void {
    const rect = this._getViewportRect();
    this._applyZoomAtPoint(delta, rect.width / 2, rect.height / 2);
  }

  private _centerImage(): void {
    const viewport = this._getViewport();
    const img = this.shadowRoot!.getElementById("svg-image") as HTMLImageElement;
    if (!viewport || !img || !img.naturalWidth) return;

    const vw = viewport.clientWidth;
    const vh = viewport.clientHeight;
    const iw = img.naturalWidth;
    const ih = img.naturalHeight;
    // not laid out yet: the resize observer fits it, once the viewport has its size
    if (!vw || !vh || !ih) return;

    const fitScale = Math.min(vw / iw, vh / ih);
    // a large diagram may need less than minScale to fit; zooming out must not enlarge it then
    this._effectiveMinScale = Math.min(this.minScale, fitScale);
    this._adjustedByUser = false;
    this._scale = fitScale;
    this._translateX = (vw - iw * fitScale) / 2;
    this._translateY = (vh - ih * fitScale) / 2;
  }

  private _clampScale(scale: number): number {
    return Math.min(this.maxScale, Math.max(this._effectiveMinScale, scale));
  }

  private _getTouchDistance(t1: Touch, t2: Touch): number {
    const dx = t1.clientX - t2.clientX;
    const dy = t1.clientY - t2.clientY;
    return Math.sqrt(dx * dx + dy * dy);
  }

  private _getViewport(): HTMLElement | null {
    return this.shadowRoot?.getElementById("viewport") ?? null;
  }

  private _getViewportRect(): DOMRect {
    return this._getViewport()!.getBoundingClientRect();
  }
}

customElements.define("svg-zoom-viewer", SvgZoomViewer);
